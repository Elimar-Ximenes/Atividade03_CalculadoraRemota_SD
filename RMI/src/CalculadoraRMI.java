import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CalculadoraRMI extends UnicastRemoteObject implements ICalculadora {

    private Calculadora calc = new Calculadora(); 
	
    protected CalculadoraRMI() throws RemoteException {
        super();
    }

    @Override
    public double soma(double oper1, double oper2) throws RemoteException {
        return calc.soma(oper1, oper2);
    }

    @Override
    public double subtrai(double oper1, double oper2) throws RemoteException {
        return calc.subtrai(oper1, oper2);
    }

    @Override
    public double multiplica(double oper1, double oper2) throws RemoteException {
        return calc.multiplica(oper1, oper2);
    }

    @Override
    public double divide(double oper1, double oper2) throws RemoteException {
        return calc.divide(oper1, oper2);
    }

    @Override
    public double calculaRPN(String expressaoRPN) throws RemoteException {
        return calc.calculaRPN(expressaoRPN);
    }
}
