package Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ImplementRemoteInterface.MontrealClass;
import ImplementRemoteInterface.OttawaClass;
import ImplementRemoteInterface.TorontoClass;

public class GlobalServer {

	public static void main(String args[]) throws Exception {
		MontrealClass montr_obj = new MontrealClass();

		int portNo = 6000;
		Registry montr_registry = LocateRegistry.createRegistry(portNo);

		montr_registry.bind("montreal_server", montr_obj);
		System.out.println("Montreal Server started on port: " + portNo);

		OttawaClass ottawa_obj = new OttawaClass();
		portNo = 6001;
		Registry ottawa_registry = LocateRegistry.createRegistry(portNo);

		ottawa_registry.bind("ottawa_server", ottawa_obj);
		System.out.println("Ottawa Server started on port: " + portNo);

		TorontoClass toronto_obj = new TorontoClass();
		portNo = 6002;
		Registry torn_registry = LocateRegistry.createRegistry(portNo);

		torn_registry.bind("toronto_server", toronto_obj);
		System.out.println("Toronto Server started on port: " + portNo);
	}
}
