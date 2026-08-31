package br.com.jadson.motocare.activities;

public class Motocicleta {

    private String id;
    private String apelido;
    private String marca;
    private String modelo;
    private String ano;
    private String placa;
    private String quilometragem;

    public Motocicleta() {
        // Construtor vazio necessário para o Firebase
    }

    public Motocicleta(
            String id,
            String apelido,
            String marca,
            String modelo,
            String ano,
            String placa,
            String quilometragem
    ) {
        this.id = id;
        this.apelido = apelido;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.placa = placa;
        this.quilometragem = quilometragem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(String quilometragem) {
        this.quilometragem = quilometragem;
    }
}