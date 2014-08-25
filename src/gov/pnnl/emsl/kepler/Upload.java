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

	public static class BuildFileMD extends SimpleFileVisitor<Path> {
		public List<gov.pnnl.emsl.SWADL.File> files;
		public List<Group> groups;
		public File parentPath;
		public BuildFileMD() {
			files = new ArrayList<gov.pnnl.emsl.SWADL.File>();
			groups = null;
			parentPath = null;
		}
		public BuildFileMD(List<Group> groups, File parentPath) {
			files = new ArrayList<gov.pnnl.emsl.SWADL.File>();
			this.groups = groups;
			this.parentPath = parentPath;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			File f = file.toFile();
			if(f.getAbsolutePath().startsWith(parentPath.getAbsolutePath()) == false)
				return FileVisitResult.TERMINATE;
			String relpath = f.getAbsolutePath().substring(parentPath.getAbsolutePath().length()+1);
			gov.pnnl.emsl.SWADL.File afmd = new gov.pnnl.emsl.SWADL.File();
			try {
				afmd.setName(relpath);
				afmd.setGroups(groups);
				afmd.setLocalName(relpath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return FileVisitResult.TERMINATE;
			}
			return FileVisitResult.CONTINUE;
		}
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
			BuildFileMD fmd = new BuildFileMD(groups, updirFile);
			Files.walkFileTree(path, fmd);

			conn.uploadWait(conn.uploadAsync(fmd.files));
		} catch (Exception ex) {
			throw new IllegalActionException(ex.toString());
		}
	}
}
