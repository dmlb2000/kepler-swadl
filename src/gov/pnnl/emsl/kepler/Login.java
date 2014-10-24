package gov.pnnl.emsl.kepler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import gov.pnnl.emsl.SWADL.SWADL;

public class Login extends TypedAtomicActor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1520L;
	public TypedIOPort myemslData;
	public TypedIOPort myemslQuery;
	public TypedIOPort proto;
	public TypedIOPort username;
	public TypedIOPort password;
	public TypedIOPort authobj;
	public TypedIOPort backend;
	public TypedIOPort zone;

	public Login(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		backend = new TypedIOPort(this, "Backend", true, false);
		backend.setTypeEquals(BaseType.STRING);
		zone = new TypedIOPort(this, "Zone", true, false);
		zone.setTypeEquals(BaseType.STRING);
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
		authobj.setTypeEquals(BaseType.OBJECT);
	}

	@Override
	public void fire() throws IllegalActionException {
		super.fire();
		SWADL conn = null;

		StringToken backendToken = (StringToken) backend.get(0);
		String backendStr = backendToken.stringValue();
		
		StringToken zoneToken = (StringToken) zone.get(0);
		String zoneStr = zoneToken.stringValue();
		
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
			if(backendStr.equals("myemsl")) {
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
				writer.close();
				conn = new gov.pnnl.emsl.PacificaLibrary.Connect(new gov.pnnl.emsl.PacificaLibrary.LibraryConfiguration(temp.getAbsolutePath()), usernameStr, passwordStr);
			} else if (backend.equals("irods")) {
				gov.pnnl.emsl.iRODS.LibraryConfiguration c = new gov.pnnl.emsl.iRODS.LibraryConfiguration();
				c.setHost(dataServerStr);
				c.setPort(1247);
				c.setZone(zoneStr);
				conn = new gov.pnnl.emsl.iRODS.Connect(c);
			}
			authobj.broadcast(new ObjectToken(conn));
		} catch(Exception ex) {
			throw new IllegalActionException(ex.toString());
		}
	}
}
