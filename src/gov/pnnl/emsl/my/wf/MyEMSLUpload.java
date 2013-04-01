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
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.ArrayToken;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.FileSystems;
import java.nio.file.attribute.BasicFileAttributes;

import java.security.NoSuchAlgorithmException;

import gov.pnnl.emsl.my.MyEMSLConnect;
import gov.pnnl.emsl.my.MyEMSLGroupMD;
import gov.pnnl.emsl.my.MyEMSLFileCollection;
import gov.pnnl.emsl.my.MyEMSLMetadata;
import gov.pnnl.emsl.my.MyEMSLFileMD;

public class MyEMSLUpload extends TypedAtomicActor {
	public TypedIOPort authobj;
	public TypedIOPort updir;
	public TypedIOPort mdobj;
	public TypedIOPort status;

	public static class BuildFileMD extends SimpleFileVisitor<Path> {
		public MyEMSLMetadata md;
		public List<MyEMSLGroupMD> groups;
		public File parentPath;
		public BuildFileMD() { md = new MyEMSLMetadata(); groups = null; parentPath = null; }
		public BuildFileMD(List<MyEMSLGroupMD> groups, File parentPath) {
			this.md = new MyEMSLMetadata();
			this.groups = groups;
			this.parentPath = parentPath;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			File f = file.toFile();
			if(f.getAbsolutePath().startsWith(parentPath.getAbsolutePath()) == false)
				return FileVisitResult.TERMINATE;
			String relpath = f.getAbsolutePath().substring(parentPath.getAbsolutePath().length()+1);
			MyEMSLFileMD afmd = new MyEMSLFileMD(relpath, f.getAbsolutePath(), "hashforfilea");
			for(MyEMSLGroupMD group: this.groups) {
				afmd.groups.add(new MyEMSLGroupMD(group.name, group.type));
			}
			md.md.file.add(afmd);
			return FileVisitResult.CONTINUE;
		}
	}

	public MyEMSLUpload(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
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
		MyEMSLConnect conn = (MyEMSLConnect) authObjToken.getValue();

		StringToken updirToken = (StringToken) updir.get(0);
		String updirStr = updirToken.stringValue();

		ArrayToken mdObjToken = (ArrayToken) mdobj.get(0);
		List<MyEMSLGroupMD> groups = new ArrayList<MyEMSLGroupMD>();
		for(int i = 0; i < mdObjToken.length(); i++)
		{
			ObjectToken mdTok = (ObjectToken) mdObjToken.getElement(i);
			groups.add((MyEMSLGroupMD)mdTok.getValue());
		}

		try {
			Path path = FileSystems.getDefault().getPath(updirStr);
			File updirFile = path.toFile();
			BuildFileMD fmd = new BuildFileMD(groups, updirFile);
			Files.walkFileTree(path, fmd);

			MyEMSLFileCollection col = new MyEMSLFileCollection(fmd.md);
			String statusURL = conn.upload(col);
			conn.status_wait(statusURL, 15, 5);
			
			status.broadcast(new StringToken(statusURL));
		} catch (IOException ex) {
			throw new IllegalActionException(ex.toString());
		} catch (SAXException ex) {
			throw new IllegalActionException(ex.toString());
		} catch (InterruptedException ex) {
			throw new IllegalActionException(ex.toString());
		} catch (XPathExpressionException ex) {
			throw new IllegalActionException(ex.toString());
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalActionException(ex.toString());
		}
	}
}
