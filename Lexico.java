package lexico;

//Paquetes externos
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import error.*;
import tabla_simbolos.*;
import token.*;

public class Lexico {

	private File archivo = null;
	private FileReader fr = null;
	private char[] a;
	private BufferedWriter bw;
	public static Integer linea = 0;//Para recorrer las lineas
	public static int indice = 0;//Para recorrer el array
	public static Long digit = (long) 0;//Para detectar digitos
	public static String cadena = "";

	/** 
	 * @funcion: lee ficheros
	 * @param fichero 
	 */
	public void leeFicheros(File fichero){
		try {
			// Apertura del fichero e inicializacion de FileReader para leerlo
			this.archivo = fichero;
			this.fr = new FileReader(archivo);
			//Leer fichero
			this.a = new char [(int) archivo.length()];//Este Array nos almacena los caracteres
			this.fr.read(a); //añade el contenido al array
			String ruta = Sintactico.miDir.getCanonicalPath()+"\\tokens.txt";
			File archivoTokens = new File(ruta);
			this.bw = new BufferedWriter(new FileWriter(archivoTokens));//leemos el fichero
			linea++;
		}catch (Exception e){
			System.out.println("Error al leer el fichero");
		}
	}

	/** 
	 * @param: caracter a comprobar
	 * @funcion: evalua si el caracter es un digito
	 */
	private boolean isDigit(char caracter){
		return caracter > 47 && caracter < 58;
	}

	/** 
	 * param: caracter a comprobar
	 * function: evalua si el caracter es una letra
	 */
	private boolean isLetter(char caracter){
		return (caracter > 64 && caracter < 91) || (caracter > 96 && caracter < 123);
	}

	/** 
	 * param: array con el contenido para crear los tokens y tabla de simbolos
	 * function: dependiendo de las condciones genera los tokens
	 */
	public Token procS(char [] contenido ,TablaSimbolos tS){
		Token toReturn = null;
		if (indice == contenido.length) {
			toReturn = new Token("EOF", null);//genera el token eof
		} else if (contenido[indice] == '/') {
			indice++;
			procA(contenido);
		} else if (contenido[indice] == '\n') {
			linea++;
			indice++;//en este caso no se genera token
		} else if (contenido[indice] == ' ' || contenido[indice] == '\t') {//tabuladores o espacios
			indice++;
		} else if (contenido[indice] == '{') {
			indice++;
			toReturn = new Token("LLAVEABIERTA",null);//genera el token LLAVEABIERTA
		} else if (contenido[indice] == '{') {
			indice++;
			toReturn = new Token("LLAVECERRADA",null);//genera el token LLAVECERRADA
		} else if (isDigit(contenido[indice])) {//si es un digito
			digit = (long) 0;
			Integer num = Character.getNumericValue(contenido[indice]);//devuelve el valor entero segun la representacion Unicode
			digit = num.longValue();
			indice++;
			procD(contenido);//concatena todos los digitos del numero

			if (digit < Math.pow(2, 15)) {//resticcion maximo numero
				toReturn = new Token("NUM",Long.toString(digit));//genera token (NUM,Valor)
			} else {
				//Error fuera de rango
			}
		} else if (contenido[indice] == '\"') {
			cadena = "";
			indice++;
			procE(contenido);
			toReturn = new Token("",cadena);//genera token (CHARS,LEXEMA)
		} else if (contenido[indice] == '+') {
			indice++;
			toReturn = new Token("SUMA",null);//genera token SUMA
		} else if (contenido[indice] == '(') {
			indice++;
			toReturn = new Token("PARENTABIERTO",null);//genera token PARARENTABIERTO
		} else if (contenido[indice] == ')') {
			indice++;
			toReturn = new Token("PARENTCERRADO",null);//genera token PARARENTCERRADO
		} else if (contenido[indice] == '<') {
			indice++;
			toReturn = new Token("MENORQUE",null);//genera token MENORQUE
		} else if (contenido[indice] == '=') {
			indice++;
			toReturn = new Token("IGUAL",null);//genera token IGUAL
		} else if (contenido[indice] == ';') {
			indice++;
			toReturn = new Token("PUNTOYCOMA",null);//genera token PUNTOYCOMA
		} else if (contenido[indice] == ',') {
			indice++;
			toReturn = new Token("COMA",null);//genera token COMA
		} 
		else if (isLetter(contenido[indice])) {
			cadena = Character.toString(contenido[indice]);//Devuelve un objeto String del caracter
			indice++;
			procG(contenido);

			if (cadena.equals("if")) {
				toReturn = new Token("IF", null);//genera token IF
			}
			else if (cadena.equals("int")) {
				toReturn = new Token("INT", null);//genera token INT
			}
			else if (cadena.equals("else")) {
				toReturn = new Token("ELSE",null);//genera token ELSE
			}
			else if (cadena.equals("write")) {
				toReturn = new Token("WRITE",null);//genera token WRITE
			}
			else if (cadena.equals("function")) {
				toReturn = new Token("FUNCTION",null);//genera token FUNCTION
			}
			else if (cadena.equals("prompt")) {
				toReturn = new Token("PROMPT",null);//genera token PROMPT
			}
			else if (cadena.equals("return")) {
				toReturn = new Token("RETURN",null);//genera token RETURN
			}
			else if (cadena.equals("var")) {
				toReturn = new Token("VAR",null);//genera token VAR
			}
			else if (cadena.equals("chars")) {
				toReturn = new Token("CHARS",null);//genera token CHARS
			} else toReturn = new Token("ID",cadena);//genera token (ID,LEXEMA)
		}
		else if (contenido[indice] == '&') {
			indice++;
			procH(contenido);
			toReturn = new Token("AND",null);//genera token AND
		}
		return toReturn;
	}

	/** 
	 * param: array con el contenido a comprobar
	 * function: detecta comentarios
	 */
	public void procA(char[] contenido) {
		if (contenido[indice] == '/') {
			indice++;
			procB(contenido);
		}
		else{
			//Error porque se esperaba /
		}
	}

	/** 
	 * param: array con el contenido a comprobar
	 * function: avanza en caso de no detecte retorno de carro y no este apuntado al final del array
	 */
	public void procB(char [] contenido) {
		if (indice < contenido.length && contenido[indice] != '\r') {
			indice++;
			procB(contenido);
		}
	}

	/** 
	 * param: array con el contenido a comprobar
	 * function: detecta digitios y los concatena 
	 */
	public void procD(char[] contenido){
		if (indice < contenido.length && isDigit(contenido[indice])) {
			digit = Character.getNumericValue(contenido[indice]) + digit*10;
			indice ++;
			procD(contenido);
		}
	}

	/** 
	 * param: array con el contenido a comprobar
	 * function: detecta letras y las concatena en una cadena
	 */
	public void procE(char[] contenido) {
		if (indice < contenido.length && contenido[indice] != '\"' && contenido[indice] != '\n' && contenido[indice] != '\r') {
			cadena += Character.toString(contenido[indice]);
			indice++;
			procE(contenido);
		} else if (indice < contenido.length && contenido[indice] == '\"') {
			indice++;
		} else {
			//Error salto de linea mientras se analizaba una cadena
		}
	}

	/**
	 * param: array con el contenido a comprobar
	 * function: detecta si los caracteres son de tipo letra o digito o _ para luego concatenarlos
	 */
	public void procG(char[] contenido) {
		if (indice < contenido.length && (isDigit(contenido[indice]) || isLetter(contenido[indice]) || contenido[indice] == '_')) {
			cadena += Character.toString(contenido[indice]);
			indice++;
			procG(contenido);
		}
	}

	/**
	 *  param: array con el contenido a comprobar
	 * 	function: comprueba si el siguiente caracter es &
	 */
	public void procH(char [] contenido) {
		if (contenido[indice] == '&') {
			toReturn = new Token("AND",null);//genera token AND
			indice++;
		} else {
			//Error se esperaba detectar el caracter &
		}

	}

	//GETTERS 
	public static int getIndice() {
		return indice;
	}

	public static Long getDigit() {
		return digit; 
	}

	public char[] getA() {
		return a;
	}

	public static String getCadena() {
		return cadena; 
	}

	public File getArchivo() {
		return archivo;
	}

	public BufferedWriter getBw() {
		return bw;
	}

	public void setBw(BufferedWriter bw) {
		this.bw = bw;
	} 

	public Token al(TablaSimbolos tabla_simbolos) {
		Token toReturn = null;
		while (toReturn == null) {
			toReturn = this.procS(this.getA(), tablaSimbolos);
		}
		this.bw.write(toReturn.toString());
		this.bw.newLine();
		return toReturn;
	}
}