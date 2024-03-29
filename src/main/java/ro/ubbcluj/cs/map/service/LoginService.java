package ro.ubbcluj.cs.map.service;

import ro.ubbcluj.cs.map.domain.Login;
import ro.ubbcluj.cs.map.repository.DBLoginRepository;

import java.util.Base64;
import java.util.Optional;

public class LoginService {
    private DBLoginRepository<Long, Login> repo;

    public LoginService(DBLoginRepository<Long, Login> repo) {
        this.repo = repo;
    }

    public Optional<Login> findOne(String email){
        return this.repo.findOne(email);
    }

    public Optional<Login> addLogin(Long idUser, String email, String password){
        Base64.Encoder encoder = Base64.getEncoder();

        Login newLogin = new Login(email, encoder.encodeToString(password.getBytes()));
        newLogin.setId(idUser);
        return this.repo.save(newLogin);
    }

    public Optional<Login> deleteLogin(Long id){
        return this.repo.delete(id);
    }

    private boolean validatePassword(Optional<Login> login, String givenPassword) {
        if(login.isEmpty()){
            return false;
        }

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(login.get().getEncryptedPassword());
        String realPassword = new String(bytes);

        if(!realPassword.equals(givenPassword)){
            return false;
        }

        return true;
    }

    public Optional<Login> tryLogin(String email, String givenPassword){
        Optional<Login> login = this.repo.findOne(email);

        if(!validatePassword(login, givenPassword)){
            return Optional.empty();
        }

        return login;
    }
}
