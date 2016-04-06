/*
 * El autor de esta aplicación no se hace responsable del mal uso de la misma. O de su uso directamente. :)
 */

package mangadownloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Victor_Reiner
 */
public class HeavenMangaProcessor {

    //constantes
    private final int NCARGAR = 20;

    //Variables constructor
    private final String path;
    private final int nDown;
    private final int maxDown;

    //Variables generales
    private String nombreSerie;

    private ArrayList<CapituloHeavenManga> listaCapitulos;
    //private ArrayList<SearchItem> listaBusquedaCapitulos;
    private int indiceEnlaces;
    private int indiceCaps;
    //Semaforos
    private Semaphore sCargarCaps;
    private Semaphore sSincroCargarCaps;
    private Semaphore sDescargarCaps;
    private Semaphore sSincroDescargarCaps;
    private Semaphore sMaxDown;
    private Semaphore sError;

    //Constructor
    public HeavenMangaProcessor(String path, int nDown, int maxDown) {
        this.nDown = nDown;
        this.maxDown = maxDown;
        this.path = path;

        this.listaCapitulos = new ArrayList<>();
        //this.listaBusquedaCapitulos = new ArrayList<>();
        this.indiceEnlaces = 0;
        this.indiceCaps = 0;
    }

    //FUNCIONES GENERALES
    //Saca el nombre de la serie
    public ArrayList<SearchItem> buscarManga(String busqueda){
        ArrayList<SearchItem> listaBusqueda = new ArrayList<>();
        String enlace = "http://heavenmanga.com/buscar/" + busqueda + ".html";
        try {
            Document doc = Jsoup.connect(enlace).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:37.0) Gecko/20100101 Firefox/37.0").timeout(8000).get();
            Elements li = doc.getElementsByAttributeValue("class","cont_manga");
            for(Element e : li){
                Elements img = e.getElementsByTag("img");
                String imagen = img.get(0).absUrl("src");
                
                
                Elements bookname = e.getElementsByTag("a");
                String url = bookname.get(0).absUrl("href");
                String nombre = img.get(0).absUrl("title");
                
                SearchItem item = new SearchItem(url, nombre, imagen);
                listaBusqueda.add(item);
            }
        } catch (IOException ex) {
            Logger.getLogger(NinemangaProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return listaBusqueda;
        
    }
    //Ejemplo enlace: http://heavenmanga.com/kamisama-drop/
    private void filtrarEnlace(String enlace) {
        String[] partes = enlace.split("/");
        String[] partes2 = partes[3].split("-");
        String aux = "";
        for (String e : partes2) {
            aux += " " + e;
        }
        
        this.nombreSerie = aux.substring(1);
        System.out.println("Se va a descargar: " + this.nombreSerie);
    }

    //Dado un enlace descarga todos sus caputulos
    public void process(String enlace) {
        filtrarEnlace(enlace);
        
        
        this.sError = new Semaphore(1);
        try {
            File folder = new File(path + "/" + this.nombreSerie);
            if (!folder.isDirectory()) {
                folder.mkdirs();
            }

            //CARGAR ENLACES
            cargarCapitulos(enlace);

            //DESCARGAR ENLACES
            //Creo Semaforos
            sDescargarCaps = new Semaphore(1);
            sMaxDown = new Semaphore(maxDown);
            sSincroDescargarCaps = new Semaphore(0);
            //Creo los hilos
            List<Thread> ths = new ArrayList<Thread>();
            for (int i = 0; i < nDown; i++) {
                Thread th = new Thread(new Runnable() {
                    public void run() {
                        try {
                            hiloDescargarCaps();
                        } catch (Exception e) {
                            try {
                                sError.acquire();
                                FileWriter ferror = new FileWriter(path + "/" + "error_log.txt", true);
                                ferror.write("Error: " + e.getMessage() + " - Causa: " + e.getCause() + "\r\n");
                                ferror.close();
                                sError.release();
                            } catch (Exception ex) {
                                e.printStackTrace();

                            }
                            sSincroDescargarCaps.release();
                        }
                    }
                }, "HiloDescargas" + i);
                th.start();
                ths.add(th);
            }

            //ESPERAMOS A QUE ESTEN TODOS LOS CAPS DESCARGADOS
            sSincroDescargarCaps.acquire(listaCapitulos.size());
            

        } catch (Exception e) {
            try {
                this.sError.acquire();
                FileWriter ferror = new FileWriter(path + "/" + "error_log.txt", true);
                ferror.write("Error: " + e.getMessage() + " - Causa: " + e.getCause() + "\r\n");
                ferror.close();
                sError.release();
            } catch (Exception ex) {
                e.printStackTrace();

            }

            System.out.println("Error en process: " + e.getMessage());
        }
    }

    //Crea y carga los capitulos y sus imagenes
    private void cargarCapitulos(String enlace) throws Exception {
        
        Document doc = Jsoup.connect(enlace).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:37.0) Gecko/20100101 Firefox/37.0").timeout(8000).get();
        int indice = 1;
        Elements listaCaps = doc.getElementsByAttributeValue("class", "manga_episodios");
        Elements capitulos = listaCaps.get(0).getElementsByTag("a");
        System.out.println("Buscando capitulos para cargarlos...");
        for (Element el : capitulos) {

            System.out.println("Capitulo nº: " + indice);
            
            indice++;
            String src = el.absUrl("href");
      
            System.out.println("Enlace del cap: " + src);
            CapituloHeavenManga cap = new CapituloHeavenManga(src);
            
            //Sacamos el capitulo
            String[] partes = el.absUrl("title").split(" ");
            String[] partes2 = cap.getSerie().split(" ");
            String capitulo = "";
            for (int k = partes2.length - 1; k <= partes.length - 1; k++) {
                capitulo += " " + partes[k];
            }
            capitulo = capitulo.substring(1);
            
            cap.setCapitulo(capitulo);
            this.listaCapitulos.add(cap);
            

        }
        //Damos la vuelta al array (casi todos los indices vienen de mas a menos)
        Collections.reverse(this.listaCapitulos);
        
        //Inicializo Semaforos
        this.sCargarCaps = new Semaphore(1);
        this.sSincroCargarCaps = new Semaphore(0);
        //Inicializo los hilos
       
        List<Thread> ths = new ArrayList<Thread>();
        for (int i = 0; i < NCARGAR; i++) {
            Thread th = new Thread(new Runnable() {
                public void run() {
                    try {
                        hiloCargarCaps();
                    } catch (Exception e) {
                        try {
                            sError.acquire();
                            FileWriter ferror = new FileWriter(path + "/" + "error_log.txt", true);
                            ferror.write("Error: " + e.getMessage() + " - Causa: " + e.getCause() + "\r\n");
                            ferror.close();
                            sError.release();
                        } catch (Exception ex) {
                            e.printStackTrace();

                        }
                        sSincroCargarCaps.release();
                    }
                }
            }, "HiloCargarImagenes" + i);
            th.start();
            ths.add(th);
        }

        //Espero a que se terminen todos los caps para terminarlo
        sSincroCargarCaps.acquire(NCARGAR);
        System.out.println("Todas las paginas de los enlaces cargados!");
    }

    private void cargarImagenesCapitulo(CapituloHeavenManga cap) throws Exception {

        Document doc = Jsoup.connect(cap.getEnlace()).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:37.0) Gecko/20100101 Firefox/37.0").timeout(8000).get();
        
        //Sacamos las imagenes
        Elements indicePaginas = doc.getElementsByAttributeValue("id", "l");
        Document pag = Jsoup.connect(indicePaginas.get(0).absUrl("href")).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:37.0) Gecko/20100101 Firefox/37.0").timeout(8000).get();
        Elements listaPaginas = pag.getElementsByTag("select");
        Elements paginas = listaPaginas.get(0).getElementsByTag("option");
        for (Element e : paginas) {
            String src = e.absUrl("value");
            
            Document con = Jsoup.connect(src).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:37.0) Gecko/20100101 Firefox/37.0").timeout(8000).get();
            Elements imagenes = con.getElementsByTag("img");

            String href = imagenes.get(1).absUrl("src");
            
            cap.agregarImagen(href);

        }

        cap.actualizarNumPaginas();
        
    }

    private void getImages(String src, String pathFolder, int indice) {
        
        //String nombreImagen = src.substring(src.lastIndexOf("/"), src.length());
        //Sacamos la extension
        //String[] partes = nombreImagen.split("[.]");
        String extension = ".jpg";
        
        // Open a URL Stream
        URL url;
        try {
            url = new URL(src);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:37.0) Gecko/20100101 Firefox/37.0");
            httpCon.setConnectTimeout(8000);
            InputStream in = httpCon.getInputStream();

            OutputStream out = new BufferedOutputStream(new FileOutputStream(pathFolder + "/" + indice + "." + extension));

            for (int b; (b = in.read()) != -1;) {
                out.write(b);
            }
            out.close();
            in.close();
        } catch (Exception ex) {
            try {
                //sSincroDescargarCaps.release();
                //sMaxDown.release();
                this.sError.acquire();
                FileWriter ferror = new FileWriter(path + "/" + "error_log.txt", true);
                ferror.write(" **** [IMG DOWNLOAD ERROR] **** \r\nURL: "
                        + src + "\r\nRUTA: "
                        + pathFolder + "\r\nIMG: "
                        + indice + "." + extension + "\r\nERROR: "
                        + ex.getMessage() + "\r\nCAUSA: "
                        + ex.getCause() + "\r\n **** [IMG DOWNLOAD ERROR] **** \r\n");
                ferror.close();
                this.sError.release();
            } catch (Exception exe) {
                this.sError.release();
                ex.printStackTrace();

            }
            System.out.println("Error: " + ex.getMessage());
        }

    }

    //FIN FUNCIONES GENERALES
    private void hiloCargarCaps() throws Exception {
        
        int indiceLocal = 0;

        
        while (indiceEnlaces <= this.listaCapitulos.size() - 1) {
            //SECCION CRITICA
            this.sCargarCaps.acquire();
            indiceLocal = this.indiceEnlaces;
            this.indiceEnlaces++;
            this.sCargarCaps.release();
            //FIN SECCION CRITICA
            
            //Carga las imagenes del capitulo
            cargarImagenesCapitulo(listaCapitulos.get(indiceLocal));
            
        }
        sSincroCargarCaps.release();
    }

    private void hiloDescargarCaps() throws Exception {
        int indiceAux;
        
        while (this.indiceCaps <= this.listaCapitulos.size() - 1) {
            //Limitador de threads a la vez
            sMaxDown.acquire();
            
            //SECCION CRITICA
            sDescargarCaps.acquire();
            indiceAux = this.indiceCaps;
            this.indiceCaps++;
            sDescargarCaps.release();
            //FIN SECCION CRITICA
            
            String pathFolder = (path + "/" + this.nombreSerie + "/" + this.listaCapitulos.get(indiceAux).getCapitulo());
            
            File folder = new File(pathFolder);
            if (!folder.isDirectory()) {
                folder.mkdirs();
            }
            System.out.println("Se va a descargar el capitulo en: " + pathFolder);
            int j = 1;
            for (String pagina : this.listaCapitulos.get(indiceAux).getListaImagenes()) {
                getImages(pagina, pathFolder, j);
                j++;
            }
            System.out.println("Capitulo: " + indiceAux +" descargado! En: " + pathFolder);
            sSincroDescargarCaps.release();
            sMaxDown.release();
        }
        sMaxDown.release();
    }
    //THREADS
    //FINTHREADS

    //OTRAS FUNCIONES
    public void mostrarListaCaps() {
        for (CapituloHeavenManga cap : this.listaCapitulos) {
            cap.mostrarCapitulo();
        }
    }
    //FIN OTRAS FUNCIONES
}
