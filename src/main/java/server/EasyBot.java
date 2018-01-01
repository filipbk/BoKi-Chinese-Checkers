package server;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import board.Pawn;
import communication.MoveRequest;

public class EasyBot extends BotStrategy{

	MoveRequest lastMove;
	@Override
	public MoveRequest getBestMove() {
		MoveRequest moveRequest = null;
		ArrayList<Point> possibleMoves = new ArrayList<>();
		Point from;
		int minDistace = Integer.MAX_VALUE;
		int newDistance;
		for(Pawn p : me.getPawns()) {
			//Pawn savedState = new Pawn(p);
			from =  p.getLocation();
			possibleMoves = game.checkValidMoves(playerID, from);
			for(Point to : possibleMoves) {
				newDistance = evaluate(to);
				if(minDistace > newDistance) {
					minDistace = newDistance;
					moveRequest = new MoveRequest(from, to);
				}
			}
		}
		lastMove = moveRequest;
		return moveRequest;
	}
	
	public MoveRequest getBestMove(ArrayList<Point> possibleMoves) {
		MoveRequest bestMove = null;
		int newDistance;
		int minDistance = Integer.MAX_VALUE;
		//ArrayList<Point> possibleMoves = game.checkValidMoves(playerID, from);
		for(Point to : possibleMoves) {
			newDistance = evaluate(to);
			if(minDistance > newDistance) {
				minDistance = newDistance;
				bestMove = new MoveRequest(lastMove.getNewPos(), to);
			}
		}
		lastMove = bestMove;
		return bestMove;
	}
	int evaluate(Point pos) {
		int target = game.getPlayer(playerID).getCorner();
		Point goal = null;
		if(target == 0) {
			goal = new Point(0, 4);
		}
		if(target == 1) {
			goal = new Point(4, 12);
		}
		if(target == 2) {
			goal = new Point(12, 16);
		}
		if(target == 3) {
			goal = new Point(16, 12);
		}
		if(target == 4) {
			goal = new Point(12, 4);
		}
		if(target == 5) {
			goal = new Point(4, 0);
		}
		
		return Math.max(Math.abs(goal.x - pos.x), Math.abs(goal.y - pos.y));
	}


	@Override
	public MoveRequest getBestMoveFromPoint(Point from) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	int getMaxScored(Point from, ArrayList<Point> possibleMoves, Pawn pawn) {
//		for(Point to : possibleMoves) {
//			game.move(playerID, from, to);
//			getMaxScored(to, game.checkValidMoves(playerID, to));
//			game.
//		}
//		int score = evaluate(from);
//		return score;
//	}
	
}
