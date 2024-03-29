package ro.ubbcluj.cs.map.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Objects;

public class User extends Entity<Long> {
    private String firstName;
    private String lastName;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);


    public User(String firstName, String lastName) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return this.getId() == ((User) o).getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), firstName, lastName);
    }

    //    @Override
//    public String toString() {
//        return  "First Name: " + firstName + "  |  " +
//                "Last Name: " + lastName + "  |  " +
//                "ID: " + id;
//    }

    @Override
    public String toString() {
        return  firstName + " " +
                lastName + "  |  " +
                "ID: " + id;
    }
}
