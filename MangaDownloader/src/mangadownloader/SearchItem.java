/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mangadownloader;

/**
 *
 * @author Reiner
 */
public class SearchItem {
    
    private String enlace;
    private String nombreSerie;
    private String imagen;
    
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
