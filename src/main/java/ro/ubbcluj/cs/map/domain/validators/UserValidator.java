package ro.ubbcluj.cs.map.domain.validators;

import ro.ubbcluj.cs.map.domain.User;

public class UserValidator implements Validator<User>{
    @Override
    public void validate(User entity) throws ValidationException {
        if(entity.getFirstName().isEmpty()){
            throw new ValidationException("First name must not be null");
        }

        if(entity.getFirstName().length() <= 2){
            throw new ValidationException("First name must have at least 3 letters");
        }

        char[] firstName = entity.getFirstName().toCharArray();
        for(char c : firstName){
            if(!Character.isLetter(c)){
                throw new ValidationException("First name must contain only letters");
            }
        }

        if(entity.getLastName().isEmpty()){
            throw new ValidationException("Last name must not be null");
        }

        if(entity.getLastName().length() <= 2){
            throw new ValidationException("Last name must have at least 3 letters");
        }

        char[] lastName = entity.getLastName().toCharArray();
        for(char c : lastName){
            if(!Character.isLetter(c)){
                throw new ValidationException("Last name must contain only letters");
            }
        }
    }
}
