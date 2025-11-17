
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CalculadoraRestClient extends JFrame {
    private JTextField operador1TextField;
    private JTextField operador2TextField;
    private JComboBox<String> operacaoComboBox;
    private JTextField resultadoTextField;

    public CalculadoraRestClient() {
        super("Calculadora REST Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        add(panel);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(inputPanel, BorderLayout.NORTH);

        inputPanel.add(new JLabel("Operador 1:"));
        operador1TextField = new JTextField(10);
        inputPanel.add(operador1TextField);

        inputPanel.add(new JLabel("Operador 2:"));
        operador2TextField = new JTextField(10);
        inputPanel.add(operador2TextField);

        inputPanel.add(new JLabel("Operação:"));
        operacaoComboBox = new JComboBox<>(new String[] {"soma", "subtração", "multiplicação", "divisão"});
        inputPanel.add(operacaoComboBox);

        JButton calcularButton = new JButton("Calcular");
        calcularButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcular();
            }
        });
        inputPanel.add(calcularButton);

        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(outputPanel, BorderLayout.SOUTH);

        outputPanel.add(new JLabel("Resultado da Operação:"));
        resultadoTextField = new JTextField(10);
        resultadoTextField.setEditable(false);
        outputPanel.add(resultadoTextField);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void calcular() {
        String operador1 = operador1TextField.getText();
        String operador2 = operador2TextField.getText();
        String urlStr = "https://calculadora-fxpc.onrender.com/operation/soma/" + operador1 + "/" + operador2;

        try {
            URL url = new URL(urlStr);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String json = reader.lines().collect(Collectors.joining());
            reader.close();

            String resultado = json.split("\"result\":")[1].split("}")[0];
            resultadoTextField.setText(resultado);
        } catch (MalformedURLException e) {
            System.out.println("URL inválida: " + urlStr);
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CalculadoraRestClient();
            }
        });
    }
}
