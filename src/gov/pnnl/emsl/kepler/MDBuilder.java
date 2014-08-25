package gov.pnnl.emsl.kepler;

import java.util.List;
import java.util.ArrayList;
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

public class MDBuilder extends TypedAtomicActor {

	public TypedIOPort inputs;
	public TypedIOPort mdobj;

	public MDBuilder(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);
		inputs = new TypedIOPort(this, "MDStrings", true, false);
		inputs.setTypeEquals(new ArrayType(BaseType.STRING));
		mdobj = new TypedIOPort(this, "MDObject", false, true);
		mdobj.setTypeEquals(new ArrayType(BaseType.OBJECT));
	}

	@Override
	public void fire() throws IllegalActionException {
		// TODO Auto-generated method stub
		super.fire();

		List<ObjectToken> groups = new ArrayList<ObjectToken>();

		ArrayToken inputToken = (ArrayToken) inputs.get(0);

		if(inputToken.length() == 0)
			throw new IllegalActionException("array length is 0 bailing\n");

		for(int i = 0; i < inputToken.length(); i++)
		{
			StringToken strTok = (StringToken) inputToken.getElement(i);
			String str = strTok.stringValue();
			String key = str.split("=")[0];
			String value = str.split("=")[1];
			groups.add(new ObjectToken(new Group(value, key)));
		}
		mdobj.broadcast(new ArrayToken(groups.toArray(new ObjectToken[0])));
	}
}
