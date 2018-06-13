package starter.start;

import generator.services.ServiceGenerator;

public class ServiceGeneratorMain {

	public static void main(String[] args) throws Exception{
		ServiceGenerator generator = new ServiceGenerator();
		generator.generate();
	}

}
