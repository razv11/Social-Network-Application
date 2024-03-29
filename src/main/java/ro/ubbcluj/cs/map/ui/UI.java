package ro.ubbcluj.cs.map.ui;

import ro.ubbcluj.cs.map.domain.Friendship;
import ro.ubbcluj.cs.map.domain.User;
import ro.ubbcluj.cs.map.domain.validators.ValidationException;
import ro.ubbcluj.cs.map.service.FriendshipService;
import ro.ubbcluj.cs.map.service.UserService;

import java.util.*;

public class UI implements UiInterface{
    private static final String RESET = "\u001B[0m";
    private static String RED = "\u001B[31m";
    private static String GREEN = "\u001B[32m";
    private static String CYAN = "\u001B[36m";

    private UserService userService;

    private FriendshipService friendshipService;

    public UI(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
    }

    private void addUser(Scanner scanner) {
        System.out.print("First Name: ");
        String first_name = scanner.nextLine();

        System.out.print("Last Name: ");
        String last_name = scanner.nextLine();

        try{
            this.userService.addUser(first_name, last_name);
            System.out.println(GREEN + "User added successfully" + RESET);
        }
        catch (ValidationException ve){
            System.out.println(ve.getMessage());
        }
        catch (IllegalArgumentException e){
            System.out.println(RED + "The ID must not be null" + RESET);
        }
    }

    private void updateUser(Scanner scanner) {
        System.out.print("User ID: ");
        String userId = scanner.nextLine();

        Long id;
        try{
            id = Long.parseLong(userId);
        }
        catch (NumberFormatException e){
            System.out.println(RED + "The ID must be numeric" + RESET);
            return;
        }

        try{
            if(this.userService.existUser(id).isEmpty()){
                System.out.println(RED + "User with id " + id.toString() + " doesn't exist" + RESET);
                return;
            }
        }
        catch (IllegalArgumentException e){
            System.out.println(RED + "The ID must not be null" + RESET);
            return;
        }

        System.out.print("New first name: ");
        String first_name = scanner.nextLine();

        System.out.print("New last name: ");
        String last_name = scanner.nextLine();

        try{
            if(this.userService.updateUser(id, first_name, last_name).isPresent()){
                System.out.println(RED + "Error: The user wasn't updated" + RESET);
                return;
            }
            System.out.println(GREEN + "The user was updated successfully" + RESET);
        }
        catch (ValidationException ve){
            System.out.println(ve.getMessage());
        }
        catch (IllegalArgumentException e){
            System.out.println(RED + "The entity is null" + RESET);
        }
    }

    private void deleteUser(Scanner scanner) {
        System.out.print("User ID: ");
        String userId = scanner.nextLine();

        Long id;
        try{
            id = Long.parseLong(userId);
        }
        catch (NumberFormatException nfe){
            System.out.println(RED + "The ID must be numeric" + RESET);
            return;
        }

        try{
            if (this.userService.deleteUser(id).isEmpty()){
                    System.out.println(RED + "There is no user with the given ID" + RESET);
                    return;
            }
            System.out.println(GREEN + "User removed successfully" + RESET);
            this.friendshipService.deleteFriendshipsByUserId(id);
        }
        catch (IllegalArgumentException ie){
            System.out.println(RED + "The ID must not be null" + RESET);
        }
    }

    private void addFriendship(Scanner scanner) {
        System.out.print("First user ID: ");
        String user1 = scanner.nextLine();

        System.out.print("Second user ID: ");
        String user2 = scanner.nextLine();

        Long id1, id2;
        try{
            id1 = Long.parseLong(user1);
            id2 = Long.parseLong(user2);
        } catch (NumberFormatException e) {
            System.out.println(RED + "The ID must be numeric" + RESET);
            return;
        }

        try{
            if(this.userService.existUser(id1).isEmpty()){
                System.out.println(RED + "User with id " + id1.toString() + " doesn't exist" + RESET);
                return;
            }

            if(this.userService.existUser(id2).isEmpty()) {
                System.out.println(RED + "User with id " + id2.toString() + " doesn't exist" + RESET);
                return;
            }
        }
        catch (IllegalArgumentException e){
            System.out.println(RED + "The ID must not be null" + RESET);
            return;
        }

        if (this.friendshipService.getFriendshipForUsers(id1, id2).isPresent()){
            System.out.println(RED + "The friendship already exists" + RESET);
            return;
        }

        this.friendshipService.addFriendship(id1, id2);
        System.out.println(GREEN + "Friendship added successfully" + RESET);
    }

    private void updateFriendship(Scanner scanner) {
        System.out.print("Friendship ID: ");
        String fId = scanner.nextLine();

        Long id;
        try{
            id = Long.parseLong(fId);
        }
        catch (NumberFormatException nfe){
            System.out.println(RED + "The ID must be numeric" + RESET);
            return;
        }

        try{
            if(this.friendshipService.findFriendshipById(id).isEmpty()){
                System.out.println(RED + "The friendship with id " + id.toString() + " doesn't exist" + GREEN);
                return;
            }
        }
        catch (IllegalArgumentException ie){
            System.out.println(RED + "The id must not be null" + RESET);
        }

        System.out.print("Number of months to be substract from the actual date: ");
        String months = scanner.nextLine();

        Long m;
        try{
            m = Long.parseLong(months);
        }
        catch (NumberFormatException nfe){
            System.out.println(RED + "The number of months must be numeric" + RESET);
            return;
        }

        long numberOfMonths = m.longValue();
        try{
            this.friendshipService.updateFriendship(id, numberOfMonths);
            System.out.println(GREEN + "The friendship was updated successfully" + RESET);
        }
        catch (IllegalArgumentException ie){
            System.out.println(RED + "The entity must not be null" + RESET);
        }
    }

    private void deleteFriendship(Scanner scanner) {
        System.out.print("Friendship ID: ");
        String friendship = scanner.nextLine();

        Long id;
        try{
            id = Long.parseLong(friendship);
        }
        catch (NumberFormatException nfe){
            System.out.println(RED + "The ID must be numeric" + RESET);
            return;
        }

        try{
            if(this.friendshipService.deleteFriendship(id).isEmpty()){
                System.out.println(RED + "The ID doesn't exist"+ RESET);
                return;
            }
            System.out.println(GREEN + "The friendship removed successfully" + RESET);
        }
        catch (IllegalArgumentException ie){
            System.out.println(RED + "The ID must not be null" + RESET);
        }
    }

    private void displayNumberOfComunities(){
        System.out.println("Number of comunities: " + this.friendshipService.countComunities(this.userService.getUsersID()));
    }

    private void displayMostSociableCommunity(){
        Set<Long> usersId = new HashSet<>();
        usersId = this.friendshipService.mostSociableCommunnity(this.userService.getUsersID());

        if(usersId.size() == 0){
            System.out.println(CYAN + "There are no communities" + RESET);
            return;
        }

        System.out.println("Most sociable community: ");
        for (User user : this.userService.getUsers()){
            if(usersId.contains(user.getId())){
                System.out.print(user.getFirstName() + " - " + user.getLastName());
                usersId.remove(user.getId());
            }
            if(usersId.size() >= 1){
                System.out.print(", ");
            }
        }
        System.out.println();
    }

    private void displayUserFriendshipsFromOneMonth(Scanner scanner){
        // read user ID
        System.out.print("User ID: ");
        String userId = scanner.nextLine();

        Long id;
        try{
            id = Long.parseLong(userId);
        }
        catch (NumberFormatException nfe){
            System.out.println(RED + "The ID must be numeric" + RESET);
            return;
        }

        if(this.userService.existUser(id).isEmpty()){
            System.out.println(RED + "The user ID doesn't exist" + RESET);
            return;
        }

        // read month
        System.out.print("Month: ");
        String monthS = scanner.nextLine();

        Long month;
        try{
            month = Long.parseLong(monthS);
        }
        catch (NumberFormatException nfe){
            System.out.println(RED + "The month must be numeric" + RESET);
            return;
        }

        // validate the month
        if(month < 1 || month > 12){
            System.out.println(RED + "The month is invalalid" + RESET);
            return;
        }

        // print the result
        List<Friendship> friendships = new ArrayList<>();
        friendships = this.friendshipService.getUserFriendshipsFromOneMonth(id, month);

        if(friendships.size() == 0){
            System.out.println(RED + "There are no friendships from month " + month + " for this user" + RESET);
            return;
        }

        System.out.println("Friendships: ");
        friendships.forEach(friendship -> {
            User user;
            if(friendship.getId1() != id) {
                user = this.userService.existUser(friendship.getId1()).get();
            }
            else {
                user = this.userService.existUser(friendship.getId2()).get();
            }

            System.out.println(user.getFirstName() + " | " + user.getLastName() + " | " + friendship.getDate());
        });
    }
    private void printUsers() {
        ArrayList<User> users = (ArrayList<User>) this.userService.getUsers();
        if (users.size() == 0){
            System.out.println(CYAN + "No users added" + RESET);
            return;
        }

        users.forEach(System.out::println);
    }

    private void printFriendships(){
        ArrayList<Friendship> friendships = (ArrayList<Friendship>) this.friendshipService.getFriendships();
        if (friendships.size() == 0){
            System.out.println(CYAN + "No friendships added" + RESET);
            return;
        }

        friendships.forEach(System.out::println);
    }

    private static void printMenu() {
        System.out.println(CYAN + "\nMenu:" + RESET);
        System.out.println("1. Add user");
        System.out.println("2. Update user");
        System.out.println("3. Delete user");
        System.out.println("=======================================");
        System.out.println("4. Add friendship");
        System.out.println("5. Update friendship (date)");
        System.out.println("6. Delete friendship");
        System.out.println("=======================================");
        System.out.println("7. Display the number of communities");
        System.out.println("8. Display the most sociable community");
        System.out.println("9. Display a user's friendships from one month");
        System.out.println("=======================================");
        System.out.println("10. Print all users");
        System.out.println("11. Print all friendships");
        System.out.println("12. Exit");
    }
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean go = true;

        while (go){
            UI.printMenu();
            System.out.print("\nOption: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1": addUser(scanner); continue;
                case "2": updateUser(scanner); continue;
                case "3": deleteUser(scanner); continue;
                case "4": addFriendship(scanner); continue;
                case "5": updateFriendship(scanner); continue;
                case "6": deleteFriendship(scanner); continue;
                case "7": displayNumberOfComunities(); continue;
                case "8": displayMostSociableCommunity(); continue;
                case "9": displayUserFriendshipsFromOneMonth(scanner); continue;
                case "10": printUsers(); continue;
                case "11": printFriendships(); continue;
                case "12": go = false; break;
                default: System.out.println(RED + "Invalid option" + RESET);
            }
        }
    }
}
