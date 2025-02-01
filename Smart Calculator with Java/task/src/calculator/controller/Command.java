package calculator.controller;

public class Command{
    private boolean exit = false;

    public boolean isCommand(String command){
        return command.startsWith("/");
    }

    public boolean isExit(){
        return exit;
    }


    public void getCommand(String command){
        switch (command) {
            case "/exit":
                exit = true;
                break;
            case "/help":
                System.out.println("The program calculates an expression");
                break;
            default:
                System.out.println("Unknown command");
                break;
        }
    }
}
