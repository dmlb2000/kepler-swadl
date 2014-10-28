package gov.pnnl.emsl.kepler;

import java.io.FileWriter;
import java.io.File;
import java.io.Writer;
import java.util.List;
import java.util.LinkedList;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.ArrayToken;
import gov.pnnl.emsl.SWADL.SWADL;


public class Download extends TypedAtomicActor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1519L;
	public TypedIOPort authobj;
	public TypedIOPort outdir;
	public TypedIOPort file;
	public TypedIOPort ofile;
	public TypedIOPort itemid;
	public TypedIOPort authtoken;

	public Download(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		authobj = new TypedIOPort(this, "MyEMSLConnection", true, false);
		authobj.setTypeEquals(BaseType.OBJECT);
		outdir = new TypedIOPort(this, "OutDir", true, false);
		outdir.setTypeEquals(BaseType.STRING);
		file = new TypedIOPort(this, "FileName", true, false);
		file.setTypeEquals(new ArrayType(BaseType.OBJECT));
		ofile = new TypedIOPort(this, "SavedFileName", false, true);
		ofile.setTypeEquals(new ArrayType(BaseType.STRING));
	}

	@Override
	public void fire() throws IllegalActionException {
		super.fire();

		ObjectToken authObjToken = (ObjectToken) authobj.get(0);
		SWADL conn = (SWADL) authObjToken.getValue();

		StringToken outDirToken = (StringToken) outdir.get(0);
		String outDirStr = (String) outDirToken.stringValue();

		ArrayToken fileToken = (ArrayToken) file.get(0);

		List<Token> savedFiles = new LinkedList<Token>();

		/* should be an array of (itemid, path, authtoken). */
		for(int i = 0; i < fileToken.length(); i++)
		{
			ObjectToken fileObjTok = (ObjectToken) fileToken.getElement(i);
			gov.pnnl.emsl.SWADL.File fileObj = (gov.pnnl.emsl.SWADL.File) fileObjTok.getValue();

			try {
				File outfile = new File(outDirStr+fileObj.getName());
				File outdir = new File(outfile.getParent());
				outdir.mkdirs();
				Writer bwout = new FileWriter(outfile);
				conn.getFile(bwout, fileObj);
				bwout.close();
				savedFiles.add(new StringToken(outDirStr+fileObj.getName()));
			} catch(Exception ex) {
				ex.printStackTrace();
				throw new IllegalActionException(ex.toString());
			}
		}
		ofile.broadcast(new ArrayToken(savedFiles.toArray(new Token[0])));
	}
}
