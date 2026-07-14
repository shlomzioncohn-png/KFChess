package view;

public class Renderer {


    //  יוצרת img חדש וטוענת לתוכו את board.png ומציגה אותו
    public void renderStaticBoard() {
        Img canvas = new Img().read("resources/board.png");
        canvas.show();
    }

    public static void main(String[] args) {
        new Renderer().renderStaticBoard();
    }
}


