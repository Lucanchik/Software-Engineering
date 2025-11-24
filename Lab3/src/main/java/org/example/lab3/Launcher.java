package org.example.lab3;

import javafx.application.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Launcher {

    private static final String INPUT_FILE = "users.txt";
    private static final ArrayList<User> users = new ArrayList<>();   // In-memory list of all valid users loaded from "users.txt" file

    public static void main (String[] args){

        Scanner s = new Scanner(System.in);
        System.out.print("Enter max attempts (n): ");
        int maxAttempts = s.nextInt();
        System.out.print("Enter block time in seconds (t): ");
        int blockTimeSec = s.nextInt();

        File readFile = new File(INPUT_FILE);
        Scanner reader = null;
        try {
            reader = new Scanner(readFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        while(reader.hasNextLine()){ // if there is a line to read -> read it
            String line =  reader.nextLine();
            String[] data = line.split(" ");
            try{
                User user = new User(data[0], data[data.length-1]); //first "split" part is email, last one is password
                users.add(user);
            }
            catch(IllegalArgumentException e){
                continue;
            }
        }

        reader.close();

        Collections.sort(users, (u1, u2) -> u1.getEmail().compareTo(u2.getEmail()));
        for(User u:users){
            System.out.println(u);
        }
        Application.launch(LoginApplication.class,String.valueOf(maxAttempts),String.valueOf(blockTimeSec));
    }

    public static boolean isKnownUser(String email) {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }


    public static boolean checkUser(String email, String password){
        for(User user : users){
            if(user.getEmail().equals(email) && user.getPassword().equals(password))return true;
        }

        return false;
    }
}
