import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICalculadora extends Remote {

    double soma(double a, double b) throws RemoteException;

    double subtrai(double a, double b) throws RemoteException;

    double multiplica(double a, double b) throws RemoteException;

    double divide(double a, double b) throws RemoteException;

    // Modo 1: servidor executa expressão completa usando RPN
    double calculaRPN(String expressaoRPN) throws RemoteException;
}
