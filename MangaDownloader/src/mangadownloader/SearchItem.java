/*
 * El autor de esta aplicación no se hace responsable del mal uso de la misma. O de su uso directamente. :)
 */

package mangadownloader;

/**
 *
 * @author Victor_Reiner
 */
public class SearchItem {
    
    private final String enlace;
    private final String nombreSerie;
    private final String imagen;
    
    public SearchItem(String enlace, String nombre, String imagen){
        this.enlace = enlace;
        this.imagen = imagen;
        this.nombreSerie = nombre;
    }

    public String getEnlace() {
        return enlace;
    }

    public String getNombreSerie() {
        return nombreSerie;
    }

    public String getImagen() {
        return imagen;
    }
    
    public void mostrarSearchItem(){
        System.out.println("---- ITEM SEARCH ----");
        System.out.println("Serie: " + this.nombreSerie);
        System.out.println("Enlace: " + this.enlace);
        System.out.println("Imagen: " + this.imagen);
        System.out.println("---- FIN ITEM SEARCH ----");
    }
}
