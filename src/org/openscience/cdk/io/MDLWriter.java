/* $RCSfile$
 * $Author$ 
 * $Date$
 * $Revision$
 * 
 * Copyright (C) 1997-2004  The Chemistry Development Kit (CDK) project
 * 
 * Contact: cdk-devel@lists.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 */
package org.openscience.cdk.io;


import org.openscience.cdk.*;
import org.openscience.cdk.exception.*;
import org.openscience.cdk.tools.IsotopeFactory;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.vecmath.*;



/**
 * Writes a molecule or an array of molecules to a MDL mol or sdf file.
 *
 * <pre>
 * MDLWriter writer = new MDLWriter(new FileWriter(new File("output.mol")));
 * writer.write((Molecule)molecule);
 * writer.close();
 * </pre>
 *
 * References:
 *   <a href="http://cdk.sf.net/biblio.html#DAL92">DAL92</a>
 *
 * @cdkPackage io
 *
 * @keyword file format, MDL molfile
 */
public class MDLWriter extends DefaultChemObjectWriter {

    private BufferedWriter writer;
    private org.openscience.cdk.tools.LoggingTool logger;
    private IsotopeFactory isotopeFactory = null;

    /**
     * Contructs a new MDLWriter that can write an array of
     * Molecules to a given OutputStream.
     *
     * @param   out  The OutputStream to write to
     */
    public MDLWriter(FileOutputStream out) throws Exception {
        this(new BufferedWriter(new OutputStreamWriter(out)));
    }

    public String getFormatName() {
        return "MDL Mol/SDF";
    }
    
    /**
     * Contructs a new MDLWriter that can write an array of 
     * Molecules to a Writer.
     *
     * @param   out  The Writer to write to
     */
    public MDLWriter(Writer out) {
        writer = new BufferedWriter(out);
        logger = new org.openscience.cdk.tools.LoggingTool(this.getClass().getName());
        try {
            isotopeFactory = IsotopeFactory.getInstance();
        } catch (Exception exception) {
            logger.error("Failed to initiate isotope factory: " + exception.toString());
        }
    }

    /**
     * Flushes the output and closes this object.
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Writes a ChemObject to the MDL molfile formated output. 
     * It can only output ChemObjects of type Molecule and
     * SetOfMolecules.
     *
     * @param object class must be of type Molecule or SetOfMolecules.
     *
     * @see ChemFile
     */
	public void write(ChemObject object) throws CDKException
	{
		if (object instanceof SetOfMolecules)
		{
		    writeSetOfMolecules((SetOfMolecules)object);
		}
		else if (object instanceof Molecule)
		{
			try{
		    		writeMolecule((Molecule)object);
			}
			catch(Exception ex){
				logger.error(ex.getMessage());
				logger.debug(ex);
			}
		}
		else
		{
		    throw new CDKException("Only supported is writing of ChemFile and Molecule objects.");
		}
	}
	
    public ChemObject highestSupportedChemObject() {
        return new SetOfMolecules();
    }

	/**
	 * Writes an array of Molecules to an OutputStream in MDL sdf format.
	 *
	 * @param   molecules  Array of Molecules that is written to an OutputStream 
	 */
	private void writeSetOfMolecules(SetOfMolecules som)
	{
		Molecule[] molecules = som.getMolecules();
			try
			{
		writeMolecule(molecules[0]);
			}
			catch (Exception exc)
			{
			}
		for (int i = 1; i <= som.getMoleculeCount() - 1; i++)
		{
			try
			{
			    writer.write("$$$$");
				writer.newLine();
				writeMolecule(molecules[i]);
			}
			catch (Exception exc)
			{
			}
		}
	}
	
	

	

	/**
	 * Writes a Molecule to an OutputStream in MDL sdf format.
	 *
	 * @param   molecule  Molecule that is written to an OutputStream 
	 */
    public void writeMolecule(Molecule molecule) throws Exception {
        int Bonorder, stereo;
        String line = "";
        
        // write header block
        String title = (String)molecule.getProperty(CDKConstants.TITLE);
        if (title == null) title = "";
        writer.write(title + "\n");
        writer.write("  CDK\n");
        String comment = (String)molecule.getProperty(CDKConstants.REMARK);
        if (comment == null) comment = "";
        writer.write(comment + "\n");
        
        // write Counts line
        line += formatMDLInt(molecule.getAtomCount(), 3);
        line += formatMDLInt(molecule.getBondCount(), 3);
        line += "  0  0  0  0  0  0  0  0999 V2000\n";
        writer.write(line);

        // write Atom block
        Atom[] atoms = molecule.getAtoms();
        for (int f = 0; f < atoms.length; f++) {
            Atom atom = atoms[f];
            line = "";
            if (atom.getPoint3D() != null) {
                line += formatMDLFloat((float) atom.getX3D());
                line += formatMDLFloat((float) atom.getY3D());
                line += formatMDLFloat((float) atom.getZ3D()) + " ";
            } else if (atom.getPoint2D() != null) {
                line += formatMDLFloat((float) atom.getX2D());
                line += formatMDLFloat((float) atom.getY2D());
                line += "    0.0000 ";
            } else {
                // if no coordinates available, then output a number
                // of zeros
                line += formatMDLFloat((float)0.0);
                line += formatMDLFloat((float)0.0);
                line += formatMDLFloat((float)0.0) + " ";
            }
            line += formatMDLString(molecule.getAtomAt(f).getSymbol(), 3);
            line += " 0  0  0  0  0  0  0  0  0  0  0  0";
            writer.write(line);
            writer.newLine();
        }

        // write Bond block
        Bond[] bonds = molecule.getBonds();
        for (int g = 0; g < bonds.length; g++) {
            Bond bond = bonds[g];
            if (bond.getAtoms().length != 2) {
                logger.warn("Skipping bond with more/less than two atoms: " + bond);
            } else {
                line = formatMDLInt(molecule.getAtomNumber(bond.getAtomAt(0)) + 1,3);
                line += formatMDLInt(molecule.getAtomNumber(bond.getAtomAt(1)) + 1,3);
                double bondOrder = bond.getOrder();
                if (bondOrder == CDKConstants.BONDORDER_AROMATIC) {
                    line += formatMDLInt(4,3);
                } else {
                    line += formatMDLInt((int)bond.getOrder(),3);
                }
                line += "  ";
                switch(bond.getStereo()){
                    case 1:
                    line += "1";
                    break;
                    case -1:
                    line += "6";
                    break;
                    default:
                    line += "0";
                }
                line += "  0  0  0 ";
                writer.write(line);
                writer.newLine();
            }
        }

        // write formal atomic charges
        for (int i = 0; i < atoms.length; i++) {
            Atom atom = atoms[i];
            int charge = atom.getFormalCharge();
            if (charge != 0) {
                writer.write("M  CHG  1 ");
                writer.write(formatMDLInt(i+1,3));
                writer.write(" ");
                writer.write(formatMDLInt(charge,3));
                writer.newLine();
            }
        }
        
        // write formal isotope information
        for (int i = 0; i < atoms.length; i++) {
            Atom atom = atoms[i];
            int atomicMass = atom.getMassNumber();
            int majorMass = isotopeFactory.getMajorIsotope(atom.getSymbol()).getMassNumber();
            if (atomicMass != 0 && atomicMass != majorMass) {
                writer.write("M  ISO  1 ");
                writer.write(formatMDLInt(i+1,3));
                writer.write(" ");
                writer.write(formatMDLInt(atomicMass - majorMass,3));
                writer.newLine();
            }
        }
        
        // close molecule
        writer.write("M  END");
        writer.newLine();
        writer.flush();
    }

	/**
	 * Formats an int to fit into the connectiontable and changes it 
     * to a String.
	 *
	 * @param   i  The int to be formated
	 * @param   l  Length of the String
	 * @return     The String to be written into the connectiontable
	 */
    private String formatMDLInt(int i, int l) {
        String s = "", fs = "";
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setParseIntegerOnly(true);
        nf.setMinimumIntegerDigits(1);
        nf.setMaximumIntegerDigits(l);
        nf.setGroupingUsed(false);
        s = nf.format(i);
        l = l - s.length();
        for (int f = 0; f < l; f++)
            fs += " ";
        fs += s;
        return fs;
    }
	
	


	/**
	 * Formats a float to fit into the connectiontable and changes it
     * to a String.
	 *
	 * @param   fl  The float to be formated
	 * @return      The String to be written into the connectiontable
	 */
    private String formatMDLFloat(float fl) {
        String s = "", fs = "";
        int l;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        nf.setMinimumIntegerDigits(1);
        nf.setMaximumIntegerDigits(4);
        nf.setMinimumFractionDigits(4);
        nf.setMaximumFractionDigits(4);
        nf.setGroupingUsed(false);
        s = nf.format(fl);
        l = 10 - s.length();
        for (int f = 0; f < l; f++)
            fs += " ";
        fs += s;
        return fs;
    }



	/**
	 * Formats a String to fit into the connectiontable.
	 *
	 * @param   s    The String to be formated
	 * @param   le   The length of the String
	 * @return       The String to be written in the connectiontable
	 */
    private String formatMDLString(String s, int le) {
        s = s.trim();
        if (s.length() > le)
            return s.substring(0, le);
        int l;
        l = le - s.length();
        for (int f = 0; f < l; f++)
            s += " ";
        return s;
    }

}


