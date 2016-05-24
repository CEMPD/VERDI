package anl.verdi.util;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import static javax.measure.units.SI.GRAM;
//import static javax.measure.units.SI.KILO;
//import static javax.measure.units.SI.KILOGRAM;
//import static javax.measure.units.SI.METER;
//import static javax.measure.units.SI.SQUARE_METER;
//
//import javax.measure.converters.ConversionException;
//import javax.measure.quantities.Quantity;
//import javax.measure.units.NonSI;
//import static javax.measure.unit.SI.GRAM;
//import static javax.measure.unit.SI.KILO;
//import static javax.measure.unit.SI.KILOGRAM;
//import static javax.measure.unit.SI.METER;
//import static javax.measure.unit.SI.SQUARE_METRE;
//
////import org.jscience.physics.measures.Measure;
//import javax.measure.Measure;
//import javax.measure.converter.ConversionException;
//import javax.measure.quantity.Quantity;
//import javax.measure.unit.NonSI;
//import javax.measure.unit.Unit;

import junit.framework.TestCase;

import org.eclipse.uomo.units.SI;
import org.eclipse.uomo.units.impl.system.Imperial;
import org.eclipse.uomo.units.impl.system.USCustomary;
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.util.VUnits;
/**
 * Tests for the units class.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class UnitsTests extends TestCase {

	public void testSupportedUnits() {
		Unit unit = VUnits.createUnit("m2");
		assertEquals(SI.SQUARE_METRE, unit);
		unit = VUnits.createUnit("km2");
		assertEquals(SI.Prefix.KILO(SI.METRE).multiply(SI.Prefix.KILO(SI.METRE)), unit);
		unit = VUnits.createUnit("ha");
		assertEquals(Imperial.ACRE.multiply(2.4711), unit);		//USCustomary.HECTARE, unit);
		unit = VUnits.createUnit("hectare");
		assertEquals(Imperial.ACRE.multiply(2.4711), unit);			//NonSI.HECTARE, unit);
		unit = VUnits.createUnit("acre");
		assertEquals(Imperial.ACRE, unit);				//NonSI.HECTARE.times(.40469), unit);

		unit = VUnits.createUnit("kg");
		assertEquals(SI.KILOGRAM, unit);
		unit = VUnits.createUnit("g");
		assertEquals(SI.GRAM, unit);
		unit = VUnits.createUnit("lb");
		assertEquals(USCustomary.POUND, unit);			//NonSI.POUND, unit);
		unit = VUnits.createUnit("lbs");
		assertEquals(USCustomary.POUND, unit);			//NonSI.POUND, unit);
		unit = VUnits.createUnit("oz");
		assertEquals(USCustomary.OUNCE, unit);			//NonSI.OUNCE, unit);

		// try these to make sure they don't throw
		// exception
		VUnits.createUnit("lbs/acre");
		VUnits.createUnit(" lbs /acre");
		VUnits.createUnit(" lbs/ acre ");
		VUnits.createUnit(" lbs / acre ");


		unit = VUnits.createUnit("foo");
		assertEquals("foo", unit.toString());
		assertEquals(VUnits.createUnit("foo"), unit);
	}

	public void testConvert() {

		Unit kgh = VUnits.createUnit("kg / hectare");
		Unit kgm = VUnits.createUnit("g / m2");

//		Quantity quant = Measure.valueOf(2, kgh);
		// 2 kilos = 2000g, 1 hectare = 10,000 square meters
//		assertEquals(2000.0 / 10000, quant.doubleValue(kgm));
		double aDbl = kgh.getConverterTo(kgm).convert(2);
		assertEquals(2000.0 / 10000.0, aDbl);

		Unit foo = VUnits.createUnit("foo");
//		quant = Measure.valueOf(2, foo);
		// can't convert to anything else
		try {
//			quant.doubleValue(kgm);
			double newValue = foo.getConverterTo(kgm).convert(2);
			fail("should have thrown conversion exception");
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			//ConversionException ex) {}
		}

		// can convert to foo
//		assertEquals(2.0, quant.doubleValue(VUnits.createUnit("foo")));
		double fooValue = foo.getConverterTo(foo).convert(2);
	}

	public void testCompatible() {
		Unit ppm = VUnits.createUnit("ppm");
		Unit kelvin = VUnits.createUnit("K");

		assertFalse(ppm.isCompatible(kelvin));
		assertTrue(ppm.isCompatible(ppm));
		assertFalse(ppm.isCompatible(VUnits.createUnit("kg / m2")));

		Unit foo = VUnits.createUnit("foo");
		assertFalse(ppm.isCompatible(foo));
	}

}
