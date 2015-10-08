package pl.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.hibernate.Session;
import org.hibernate.Transaction;
import pl.Main;
import pl.MessageBox;
import pl.jpa.SessionUtil;

public class ReadyCashController {

    @FXML
    private TextField amountField;

    @FXML
    public void initialize() {
        amountField.setText(Float.toString(Main.getLoggedUser().getReadycash().getMoney()));
    }

    @FXML
    private void handleSave() {
        try {
            checkAllField();
            Main.getLoggedUser().getReadycash().setMoney(Float.valueOf(amountField.getText()));
            Session session = SessionUtil.getSession();
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(Main.getLoggedUser().getReadycash());
            tx.commit();
            session.close();
            MessageBox.showInformationMessage("Információ", "Mentve!", "Sikeresen elmentette az aktuális állapotot!", false);
        } catch (Throwable ex) {
            MessageBox.showErrorMessage("Hiba", "Nem lehet elmenteni az aktuális állapotot!", ex.getMessage(), false);
        }
    }

    private void checkAllField() throws Exception {
        StringBuffer buffer = new StringBuffer();

        try {
            if( Float.valueOf(amountField.getText()) < 0 ) {
                buffer.append("Nem lehet az összeg 0-nál kisebb!\n");
            }
        } catch (NumberFormatException ex) {
            buffer.append("Csak számot lehet beírni a mezőbe!\n");
        }

        if( buffer.toString().length() != 0 ) {
            throw new Exception(buffer.toString());
        }
    }

}
