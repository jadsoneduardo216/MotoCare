
package br.com.jadson.motocare.activities;

public class TrocaOleo {

    private String id;
    private String idMoto;
    private String uidUsuario;
    private String data;
    private String quilometragem;
    private int intervaloKm;
    private boolean sincronizado;

    // Construtor vazio necessário para o Firebase
    public TrocaOleo() {
    }

    // Construtor completo
    public TrocaOleo(
            String id,
            String idMoto,
            String uidUsuario,
            String data,
            String quilometragem,
            int intervaloKm,
            boolean sincronizado
    ) {
        this.id = id;
        this.idMoto = idMoto;
        this.uidUsuario = uidUsuario;
        this.data = data;
        this.quilometragem = quilometragem;
        this.intervaloKm = intervaloKm;
        this.sincronizado = sincronizado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdMoto() {
        return idMoto;
    }

    public void setIdMoto(String idMoto) {
        this.idMoto = idMoto;
    }

    public String getUidUsuario() {
        return uidUsuario;
    }

    public void setUidUsuario(String uidUsuario) {
        this.uidUsuario = uidUsuario;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(String quilometragem) {
        this.quilometragem = quilometragem;
    }

    public int getIntervaloKm() {
        return intervaloKm;
    }

    public void setIntervaloKm(int intervaloKm) {
        this.intervaloKm = intervaloKm;
    }

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }
}
