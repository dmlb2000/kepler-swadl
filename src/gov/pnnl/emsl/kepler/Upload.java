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

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.FileSystems;
import java.nio.file.attribute.BasicFileAttributes;

import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.SWADL;

public class Upload extends TypedAtomicActor {
	public TypedIOPort authobj;
	public TypedIOPort updir;
	public TypedIOPort mdobj;
	public TypedIOPort status;

	public List<gov.pnnl.emsl.SWADL.File> getFiles(String folder, List<Group> groups) {

		List<gov.pnnl.emsl.SWADL.File> list = new ArrayList<gov.pnnl.emsl.SWADL.File>();
		File dir = new File(folder);
		if(dir.isDirectory()) {
			File[] fileNames = dir.listFiles();
			for (File file : fileNames) {
				String s = file.getName();
				gov.pnnl.emsl.SWADL.File f = new gov.pnnl.emsl.SWADL.File();
				f.setLocalName(s);
				f.setName(s);
				f.setGroups(groups);
				list.add(f);
				if(file.isDirectory())
					list.add(getFiles(file.getName(), groups));
			}
		}
		return list;
	}

	public Upload(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		authobj = new TypedIOPort(this, "MyEMSLConnection", true, false);
		authobj.setTypeEquals(BaseType.OBJECT);
		mdobj = new TypedIOPort(this, "MyEMSLMetadata", true, false);
		mdobj.setTypeEquals(new ArrayType(BaseType.OBJECT));
		updir = new TypedIOPort(this, "MyEMSLUploadDir", true, false);
		updir.setTypeEquals(BaseType.STRING);
		status = new TypedIOPort(this, "MyEMSLStatusURL", false, true);
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

		try {
			Path path = FileSystems.getDefault().getPath(updirStr);
			File updirFile = path.toFile();

			conn.uploadWait(conn.uploadAsync(getFiles(updirFile.getName()));
		} catch (Exception ex) {
			throw new IllegalActionException(ex.toString());
		}
	}
}
