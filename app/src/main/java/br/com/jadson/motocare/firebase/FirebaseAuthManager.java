package br.com.jadson.motocare.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Retorna o usuário atualmente autenticado.
     * Se não houver usuário logado, retorna null.
     */
    public FirebaseUser getUsuarioAtual() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Verifica se existe um usuário autenticado.
     */
    public boolean usuarioEstaLogado() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Retorna o UID do usuário atual.
     */
    public String getUidUsuarioAtual() {
        FirebaseUser usuario = firebaseAuth.getCurrentUser();

        if (usuario != null) {
            return usuario.getUid();
        }

        return null;
    }

    /**
     * Encerra a sessão do usuário.
     */
    public void logout() {
        firebaseAuth.signOut();
    }
}