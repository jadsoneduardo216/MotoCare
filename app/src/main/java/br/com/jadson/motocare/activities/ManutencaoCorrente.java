package br.com.jadson.motocare.activities;

import java.util.UUID;

public class ManutencaoCorrente {

    private String id;
    private String idMoto;
    private String uidUsuario;
    private String data;
    private String quilometragem;
    private boolean sincronizado;

    public ManutencaoCorrente() {
        // Construtor vazio
    }

    public ManutencaoCorrente(
            String idMoto,
            String uidUsuario,
            String data,
            String quilometragem
    ) {
        this.id = UUID.randomUUID().toString();
        this.idMoto = idMoto;
        this.uidUsuario = uidUsuario;
        this.data = data;
        this.quilometragem = quilometragem;
        this.sincronizado = false;
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

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }
}