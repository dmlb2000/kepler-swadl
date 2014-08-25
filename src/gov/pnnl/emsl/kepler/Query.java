package gov.pnnl.emsl.kepler;

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
import ptolemy.data.ArrayToken;
import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.SWADL;

public class Query extends TypedAtomicActor {
	public TypedIOPort authobj;
	public TypedIOPort mdobj;
	public TypedIOPort file;
	public TypedIOPort itemid;
	public TypedIOPort authtoken;

	public Query(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		authobj = new TypedIOPort(this, "MyEMSLConnection", true, false);
		authobj.setTypeEquals(BaseType.OBJECT);
		mdobj = new TypedIOPort(this, "MyEMSLMetadata", true, false);
		mdobj.setTypeEquals(new ArrayType(BaseType.OBJECT));
		file = new TypedIOPort(this, "FileName", false, true);
		file.setTypeEquals(new ArrayType(BaseType.OBJECT));
	}

	@Override
	public void fire() throws IllegalActionException {
		// TODO Auto-generated method stub
		super.fire();

		ObjectToken authObjToken = (ObjectToken) authobj.get(0);
		SWADL conn = (SWADL) authObjToken.getValue();

		ArrayToken mdObjToken = (ArrayToken) mdobj.get(0);
		List<Group> groups = new ArrayList<Group>();
		for(int i = 0; i < mdObjToken.length(); i++)
		{
			ObjectToken mdTok = (ObjectToken) mdObjToken.getElement(i);
			groups.add((Group)mdTok.getValue());
		}

		List<gov.pnnl.emsl.SWADL.File> files = new ArrayList<gov.pnnl.emsl.SWADL.File>();
		
		try {
			/* should be an array of (itemid, path, authtoken). */
			for(gov.pnnl.emsl.SWADL.File item:conn.query(groups))
			{
				files.add(item);
			}
			file.broadcast(new ArrayToken(files.toArray(new ObjectToken[0])));
		} catch(Exception ex) {
			throw new IllegalActionException(ex.toString());
		}
	}
}
