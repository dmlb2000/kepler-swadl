package gov.pnnl.emsl.my.wf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import javax.xml.parsers.ParserConfigurationException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;

import gov.pnnl.emsl.my.MyEMSLConnect;
import gov.pnnl.emsl.my.MyEMSLConfig;

public class MyEMSLLogin extends TypedAtomicActor {
	public TypedIOPort myemslData;
	public TypedIOPort myemslQuery;
	public TypedIOPort proto;
	public TypedIOPort username;
	public TypedIOPort password;
	public TypedIOPort authobj;

	public MyEMSLLogin(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		myemslData = new TypedIOPort(this, "DataServer", true, false);
		myemslData.setTypeEquals(BaseType.STRING);
		myemslQuery = new TypedIOPort(this, "QueryServer", true, false);
		myemslQuery.setTypeEquals(BaseType.STRING);
		proto = new TypedIOPort(this, "Proto", true, false);
		proto.setTypeEquals(BaseType.STRING);
		username = new TypedIOPort(this, "Username", true, false);
		username.setTypeEquals(BaseType.STRING);
		password = new TypedIOPort(this, "Password", true, false);
		password.setTypeEquals(BaseType.STRING);
		authobj = new TypedIOPort(this, "Connection", false, true);
		authobj.setTypeEquals(BaseType.UNKNOWN);
	}

	@Override
	public void fire() throws IllegalActionException {
		// TODO Auto-generated method stub
		super.fire();

		StringToken dataServerToken = (StringToken) myemslData.get(0);
		String dataServerStr = dataServerToken.stringValue();

		StringToken queryServerToken = (StringToken) myemslQuery.get(0);
		String queryServerStr = queryServerToken.stringValue();

		StringToken protoToken = (StringToken) proto.get(0);
		String protoStr = protoToken.stringValue();

		StringToken usernameToken = (StringToken) username.get(0);
		String usernameStr = usernameToken.stringValue();

		StringToken passwordToken = (StringToken) password.get(0);
		String passwordStr = passwordToken.stringValue();

		try {
			File temp = File.createTempFile("temp",".ini");
			temp.deleteOnExit();
			BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
			writer.write("[client]\nproto=");
			writer.write(protoStr);
			writer.write("\nquery_server=");
			writer.write(queryServerStr);
			writer.write("\nserver=");
			writer.write(dataServerStr);
			writer.write("\nservices=myemsl/services\n");
			authobj.broadcast(new ObjectToken(new MyEMSLConnect(new MyEMSLConfig(temp.getAbsolutePath()), usernameStr, passwordStr)));
		} catch(IOException ex) {
			throw new IllegalActionException(ex.toString());
		} catch(GeneralSecurityException ex) {
			throw new IllegalActionException(ex.toString());
		} catch(URISyntaxException ex) {
			throw new IllegalActionException(ex.toString());
		} catch(ParserConfigurationException ex) {
			throw new IllegalActionException(ex.toString());
		}
	}
}
