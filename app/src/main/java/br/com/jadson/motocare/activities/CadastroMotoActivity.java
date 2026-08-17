package br.com.jadson.motocare.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.com.jadson.motocare.R;

public class CadastroMotoActivity extends AppCompatActivity {

    private EditText edtApelido;
    private EditText edtMarca;
    private EditText edtModelo;
    private EditText edtAno;
    private EditText edtPlaca;
    private EditText edtQuilometragem;

    private TextView btnSalvarMoto;
    private ImageView btnVoltar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cadastro_moto);

        inicializarViews();
        configurarBotoes();
    }

    private void inicializarViews() {

        edtApelido = findViewById(R.id.edtApelido);
        edtMarca = findViewById(R.id.edtMarca);
        edtModelo = findViewById(R.id.edtModelo);
        edtAno = findViewById(R.id.edtAno);
        edtPlaca = findViewById(R.id.edtPlaca);
        edtQuilometragem = findViewById(R.id.edtQuilometragem);

        btnSalvarMoto = findViewById(R.id.btnSalvarMoto);
        btnVoltar = findViewById(R.id.btnVoltar);
    }

    private void configurarBotoes() {

        btnVoltar.setOnClickListener(v -> finish());

        btnSalvarMoto.setOnClickListener(v -> salvarMotocicleta());
    }

    private void salvarMotocicleta() {

        String apelido = edtApelido.getText().toString().trim();
        String marca = edtMarca.getText().toString().trim();
        String modelo = edtModelo.getText().toString().trim();
        String ano = edtAno.getText().toString().trim();
        String placa = edtPlaca.getText().toString().trim();
        String quilometragem = edtQuilometragem.getText().toString().trim();

        if (apelido.isEmpty()) {
            edtApelido.setError("Informe um apelido para sua moto");
            edtApelido.requestFocus();
            return;
        }

        if (marca.isEmpty()) {
            edtMarca.setError("Informe a marca");
            edtMarca.requestFocus();
            return;
        }

        if (modelo.isEmpty()) {
            edtModelo.setError("Informe o modelo");
            edtModelo.requestFocus();
            return;
        }

        if (ano.isEmpty()) {
            edtAno.setError("Informe o ano");
            edtAno.requestFocus();
            return;
        }

        if (placa.isEmpty()) {
            edtPlaca.setError("Informe a placa");
            edtPlaca.requestFocus();
            return;
        }

        if (quilometragem.isEmpty()) {
            edtQuilometragem.setError("Informe a quilometragem atual");
            edtQuilometragem.requestFocus();
            return;
        }

        Toast.makeText(
                this,
                "Motocicleta preenchida com sucesso!",
                Toast.LENGTH_SHORT
        ).show();
    }
}