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
import ptolemy.data.type.Type;
import ptolemy.data.ObjectToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import gov.pnnl.emsl.SWADL.Group;
import gov.pnnl.emsl.SWADL.SWADL;

public class Query extends TypedAtomicActor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1522L;
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
		file.setTypeEquals(BaseType.OBJECT);
	}

	@Override
	public void fire() throws IllegalActionException {
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

		
		try {
			for(gov.pnnl.emsl.SWADL.File f: conn.query(groups))
			{
				file.broadcast(new ObjectToken(f));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new IllegalActionException(ex.toString());
		}
	}
}
