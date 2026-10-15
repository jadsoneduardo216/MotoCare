package br.com.jadson.motocare.activities;

public class Manutencao {

    private String id;
    private String idMoto;
    private String uidUsuario;
    private String tipo;
    private String descricao;
    private String data;
    private String quilometragem;
    private String valor;
    private boolean sincronizado;

    public Manutencao() {
        // Construtor vazio necessário para o Firebase
    }

    public Manutencao(
            String id,
            String idMoto,
            String uidUsuario,
            String tipo,
            String descricao,
            String data,
            String quilometragem,
            String valor,
            boolean sincronizado
    ) {
        this.id = id;
        this.idMoto = idMoto;
        this.uidUsuario = uidUsuario;
        this.tipo = tipo;
        this.descricao = descricao;
        this.data = data;
        this.quilometragem = quilometragem;
        this.valor = valor;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }
}