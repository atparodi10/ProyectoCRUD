module ni.edu.uam.practicacrud {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ni.edu.uam.practicacrud to javafx.fxml;
    exports ni.edu.uam.practicacrud;


    opens ni.edu.uam.practicacrud.controller to javafx.fxml;
    exports ni.edu.uam.practicacrud.controller;


    opens ni.edu.uam.practicacrud.model to javafx.base;
    exports ni.edu.uam.practicacrud.model;

    exports ni.edu.uam.practicacrud.dao;
}