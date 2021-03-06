package server;

import java.util.ArrayList;

import board.Pawn;


/**
 * @author filipbk
 * Class with information about player's pawns, client
 * and starting corner on board
 *
 */
public class Player {
	
	private ArrayList<Pawn> pawns;
	private int clientID;
	private int corner;
	private MoveType moveType;
	
	public enum MoveType {FIRST, NEXT};
	
	public Player(int clientID, int corner) {
		this.clientID = clientID;
		this.corner = corner;
		this.moveType = MoveType.FIRST;
	}
	
	public void setPawns(ArrayList<Pawn> pawns) {
		this.pawns = pawns;
	}
	
	public ArrayList<Pawn> getPawns() {
		return pawns;
	}
	
	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	
	public int getClientID() {
		return clientID;
	}
	
	public void setCorner(int corner) {
		this.corner = corner;
	}
	
	public int getCorner() {
		return corner;
	}
	
	
	/**
	 * @return false if any of Pawns is not in target,
	 * true otherwise
	 */
	public boolean hasFinished() {
		for(Pawn pawn : pawns) {
			if(!pawn.isInTarget()) {
				return false;
			}
		}
		return true;
	}
	
	public void setMoveType(MoveType moveType) {
		this.moveType = moveType;
	}
	
	public MoveType getMoveType() {
		return moveType;
	}
	
	public void endTurn() {
		for(Pawn p : pawns) {
			p.setNotInMove();
			p.setNotAfterStep();
		}
	}

}
