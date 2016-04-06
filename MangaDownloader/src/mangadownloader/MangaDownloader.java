/*
 * El autor de esta aplicación no se hace responsable del mal uso de la misma. O de su uso directamente. :)
 */
package mangadownloader;

import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Victor_Reiner
 */
public class MangaDownloader {

    public static void main(String[] args) {
        
        System.out.println("Introduzca en enlace al indice de capitulos ");
        System.out.println("*Ejemplo: http://es.ninemanga.com/manga/Prison%20School.html");
        Scanner in = new Scanner(System.in);
        String enlace = in.nextLine();
        
        
        NinemangaProcessor procesador = new NinemangaProcessor("./downloads", 20, 20);
        /*ArrayList<SearchItem> searchs = new ArrayList<>();
        searchs = procesador.buscarManga(enlace);
        for(SearchItem e : searchs){
            e.mostrarSearchItem();
        }*/
        //System.out.println("Procediendo a decargar imagenes:");
        //Ejemplo: "http://es.ninemanga.com/manga/Nobunaga%20no%20Chef.html"
        
        procesador.process(enlace);
       
    }
    
}
