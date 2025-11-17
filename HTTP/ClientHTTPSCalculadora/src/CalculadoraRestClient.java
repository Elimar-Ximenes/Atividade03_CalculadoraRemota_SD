import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.stream.Collectors;

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
        operacaoComboBox = new JComboBox<>(new String[]{"soma", "subtração", "multiplicação", "divisão"});
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
        try {
            String operador1 = operador1TextField.getText();
            String operador2 = operador2TextField.getText();

            int operacao = operacaoComboBox.getSelectedIndex() + 1;

            URI uri = new URI("http://localhost:8080/calculadora.php");
            URL url = uri.toURL(); 
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String params = "oper1=" + operador1 +
                            "&oper2=" + operador2 +
                            "&operacao=" + operacao;

            conn.getOutputStream().write(params.getBytes());

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            String json = reader.lines().collect(Collectors.joining());
            reader.close();

            // Extrair resultado do JSON
            String resultado = json.split("\"resultado\":")[1]
                                   .split("}")[0]
                                   .replace(":", "")
                                   .trim();

            resultadoTextField.setText(resultado);

        } catch (IOException e) {
            resultadoTextField.setText("Erro!");
            e.printStackTrace();
        } catch (Exception e) {
            resultadoTextField.setText("Erro!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new CalculadoraRestClient());
    }
}
