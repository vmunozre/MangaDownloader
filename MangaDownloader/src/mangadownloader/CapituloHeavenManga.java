/*
 * El autor de esta aplicación no se hace responsable del mal uso de la misma. O de su uso directamente. :)
 */
package mangadownloader;

import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Victor_Reiner
 */
public class CapituloHeavenManga {

    private String serie;
    private String capitulo;
    private float numCap;
    private final String enlace;
    private int numPaginas;
    private String primeraPagina;   //Enlace a la primera paginas
    private ArrayList<String> listaImagenes;
    //http://heavenmanga.com/kamisama-drop-8.html
    public CapituloHeavenManga(String enlace) {
        this.enlace = enlace;
        listaImagenes = new ArrayList<>();

        extraerDatosEnlace(this.enlace);
        this.capitulo = "";
        this.numCap = 0;
        this.numPaginas = 0;
    }

    private void extraerDatosEnlace(String enlace) {
        String[] partes = enlace.split("/");
        String[] partes2 = partes[3].split("-");
        partes2[partes2.length-1] = "";
        for (String e : partes2) {
            this.serie += " " + e;
        }
        
        this.serie = this.serie.substring(1);
        //this.serie = partes2[0] + " " + partes[1];
    }

    public void mostrarCapitulo() {
        System.out.println("-------- INICIO DEL CAPITULO -------");
        System.out.println("Enlace: " + enlace);
        System.out.println("Capitulo: " + capitulo);
        System.out.println("Serie: " + serie);
        System.out.println("NumCap: " + numCap);
        System.out.println("NumPaginas: " + numPaginas);
        System.out.println("Primera pagina: " + primeraPagina);
        System.out.println("Imagenes del cap:");
        for (String e : listaImagenes) {
            System.out.println("Imagen: " + e);
        }
        System.out.println("-------- FIN DEL CAPITULO -------");
    }

    public void actualizarNumPaginas() {
        this.numPaginas = listaImagenes.size();
    }

    public void agregarImagen(String src) {
        this.listaImagenes.add(src);
    }

    public String getSerie() {
        return serie;
    }

    public String getCapitulo() {
        return capitulo;
    }

    public float getNumCap() {
        return numCap;
    }

    public String getEnlace() {
        return enlace;
    }

    public int getNumPaginas() {
        return numPaginas;
    }

    public String getPrimeraPagina() {
        return primeraPagina;
    }

    public ArrayList<String> getListaImagenes() {
        return listaImagenes;
    }
    public void setNumCap(float numCap){
        this.numCap = numCap;
    }
    public void setCapitulo(String capitulo){
        this.capitulo = capitulo;
    }

}
