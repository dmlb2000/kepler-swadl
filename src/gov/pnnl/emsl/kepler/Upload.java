package gov.pnnl.emsl.kepler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.ArrayToken;

import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.SWADL;

public class Upload extends TypedAtomicActor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1523L;
	public TypedIOPort authobj;
	public TypedIOPort updir;
	public TypedIOPort mdobj;
	public TypedIOPort status;

	public List<gov.pnnl.emsl.SWADL.File> getFiles(String folder, List<Group> groups) throws Exception {

		List<gov.pnnl.emsl.SWADL.File> list = new ArrayList<gov.pnnl.emsl.SWADL.File>();
		System.out.print("processing ("+folder+")\n");
		File dir = new File(folder);
		if(dir.isDirectory()) {
			System.out.print("its a folder\n");
			File[] fileNames = dir.listFiles();
			for (File file : fileNames) {
				String s = file.getName();
				System.out.print("processing ("+s+")\n");
				gov.pnnl.emsl.SWADL.File f = new gov.pnnl.emsl.SWADL.File();
				f.setLocalName(s);
				f.setName(s);
				f.setGroups(groups);
				list.add(f);
				if(file.isDirectory()) {
					list.addAll(this.getFiles(file.getName(), groups));
				}
			}
		}
		return list;
	}

	public Upload(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		authobj = new TypedIOPort(this, "Connection", true, false);
		authobj.setTypeEquals(BaseType.OBJECT);
		mdobj = new TypedIOPort(this, "Metadata", true, false);
		mdobj.setTypeEquals(new ArrayType(BaseType.OBJECT));
		updir = new TypedIOPort(this, "UploadDir", true, false);
		updir.setTypeEquals(BaseType.STRING);
		status = new TypedIOPort(this, "StatusURL", false, true);
		status.setTypeEquals(BaseType.STRING);
	}

	@Override
	public void fire() throws IllegalActionException {
		super.fire();
		ObjectToken authObjToken = (ObjectToken) authobj.get(0);
		SWADL conn = (SWADL) authObjToken.getValue();

		StringToken updirToken = (StringToken) updir.get(0);
		String updirStr = updirToken.stringValue();

		ArrayToken mdObjToken = (ArrayToken) mdobj.get(0);
		List<Group> groups = new ArrayList<Group>();
		for(int i = 0; i < mdObjToken.length(); i++)
		{
			ObjectToken mdTok = (ObjectToken) mdObjToken.getElement(i);
			groups.add((Group)mdTok.getValue());
		}
		File updirFile = null;
		try {
			updirFile = new File(updirStr);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalActionException(ex.toString());
		}
		List<gov.pnnl.emsl.SWADL.File> f = null;
		try {
			System.out.print(updirFile.getName());
			f = this.getFiles(updirFile.getName(), groups);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalActionException(ex.toString());
		}
		gov.pnnl.emsl.SWADL.UploadHandle h = null;
		try {
			System.out.print("blah and more blah\n");
			for(gov.pnnl.emsl.SWADL.File a: f) {
				System.out.print("blah("+a.getName()+")blah\n");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalActionException(ex.toString());
		}
		try {
			h = conn.uploadAsync(f);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalActionException(ex.toString());
		}
		try {
			conn.uploadWait(h);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalActionException(ex.toString());
		}
		try {
			status.broadcast(new StringToken("done"));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalActionException(ex.toString());
		}
	}
}
