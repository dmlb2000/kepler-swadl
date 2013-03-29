package gov.pnnl.emsl.my.wf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

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

import gov.pnnl.emsl.my.MyEMSLConnect;
import gov.pnnl.emsl.my.MyEMSLGroupMD;

public class MyEMSLQuery extends TypedAtomicActor {
	public TypedIOPort authobj;
	public TypedIOPort outdir;
	public TypedIOPort file;
	public TypedIOPort itemid;
	public TypedIOPort authtoken;

	public MyEMSLQuery(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		authobj = new TypedIOPort(this, "MyEMSLConnection", true, false);
		authobj.setTypeEquals(BaseType.UNKNOWN);
		outdir = new TypedIOPort(this, "OutDir", true, false);
		outdir.setTypeEquals(BaseType.String);
		itemid = new TypedIOPort(this, "ItemID", true, false);
		itemid.setTypeEquals(new ArrayType(BaseType.INT));
		authtoken = new TypedIOPort(this, "AuthToken", true, false);
		authtoken.setTypeEquals(new ArrayType(BaseType.STRING));
		file = new TypedIOPort(this, "FileName", true, false);
		file.setTypeEquals(new ArrayType(BaseType.STRING));
	}

	@Override
	public void fire() throws IllegalActionException {
		// TODO Auto-generated method stub
		super.fire();

		ObjectToken authObjToken = (ObjectToken) authobj.get(0);
		MyEMSLConnect conn = (MyEMSLConnect) authObjToken.getValue();

		StringToken outDirToken = (StringToken) outdir.get(0);
		String outDirStr = (String) outDirToken.getString();

		ArrayToken itemidToken = (ArrayToken) itemid.get(0);
		ArrayToken authtToken = (ArrayToken) authtoken.get(0);
		ArrayToken fileToken = (ArrayToken) file.get(0);

		if(itemidToken.length() != authtToken.length() && authtToken.length() != fileToken.length())
		{
			throw new IllegalActionException("itemid authtoken and file arrays don't have the same length");
		}

		/* should be an array of (itemid, path, authtoken). */
		for(int i = 0; i < fileToken.length(); i++)
		{
			StringToken fileStrTok = (StringToken) fileToken.getElement(i);
			String fileStr = fileStrTok.getString();
			StringToken authtStrTok = (StringToken) authtToken.getElement(i);
			String authtStr = authtStrTok.getString();
			IntToken itemidIntTok = (IntToken) itemidToken.getElement(i);
			Integer itemidInt = itemidStrTok.getInt();

			File outfile = new File(outDirStr+fileStr);
			File outdir = new File(outfile.getParent());
			try {
				outdir.mkdirs();
				BufferedWriter bwout = new BufferedWriter(new FileWriter(outfile));
				conn.getitem(bwout, new Triplet<Integer,String,String>(itemidInt,fileStr,authtStr));
				bwout.close();
			} catch(XPathExpressionException ex) {
				throw new IllegalActionException(ex.toString());
			} catch(SAXException ex) {
				throw new IllegalActionException(ex.toString());
			} catch(IOException ex) {
				throw new IllegalActionException(ex.toString());
			}
		}
	}
}
