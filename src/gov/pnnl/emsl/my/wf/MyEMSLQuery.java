package gov.pnnl.emsl.my.wf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import org.javatuples.Triplet;

import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPathExpressionException;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.IntToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;

import gov.pnnl.emsl.my.MyEMSLConnect;
import gov.pnnl.emsl.my.MyEMSLGroupMD;

public class MyEMSLQuery extends TypedAtomicActor {
	public TypedIOPort authobj;
	public TypedIOPort mdobj;
	public TypedIOPort file;
	public TypedIOPort itemid;
	public TypedIOPort authtoken;

	public MyEMSLQuery(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		authobj = new TypedIOPort(this, "MyEMSLConnection", true, false);
		authobj.setTypeEquals(BaseType.UNKNOWN);
		mdobj = new TypedIOPort(this, "MyEMSLMetadata", true, false);
		mdobj.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
		itemid = new TypedIOPort(this, "ItemID", false, true);
		itemid.setTypeEquals(new ArrayType(BaseType.INT));
		authtoken = new TypedIOPort(this, "AuthToken", false, true);
		authtoken.setTypeEquals(new ArrayType(BaseType.STRING));
		file = new TypedIOPort(this, "FileName", false, true);
		file.setTypeEquals(new ArrayType(BaseType.STRING));
	}

	@Override
	public void fire() throws IllegalActionException {
		// TODO Auto-generated method stub
		super.fire();

		ObjectToken authObjToken = (ObjectToken) authobj.get(0);
		MyEMSLConnect conn = (MyEMSLConnect) authObjToken.getValue();

		ObjectToken mdObjToken = (ObjectToken) mdobj.get(0);
		List<MyEMSLGroupMD> groups = (List<MyEMSLGroupMD>) mdObjToken.getValue();

		List<Token> itemids = new LinkedList<Token>();
		List<Token> files = new LinkedList<Token>();
		List<Token> authtokens = new LinkedList<Token>();

		try {
			/* should be an array of (itemid, path, authtoken). */
			for(Triplet<Integer,String,String> item:conn.query(groups))
			{
				files.add(new StringToken(item.getValue1()));
				itemids.add(new IntToken(item.getValue0()));
				authtokens.add(new StringToken(item.getValue2()));
			}
			file.broadcast(new ArrayToken(files.toArray(new Token[0])));
			authtoken.broadcast(new ArrayToken(authtokens.toArray(new Token[0])));
			itemid.broadcast(new ArrayToken(itemids.toArray(new Token[0])));
		} catch(XPathExpressionException ex) {
			throw new IllegalActionException(ex.toString());
		} catch(SAXException ex) {
			throw new IllegalActionException(ex.toString());
		} catch(IOException ex) {
			throw new IllegalActionException(ex.toString());
		}
	}
}
