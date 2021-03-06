package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import board.BoardSixFour;
import board.BoardSixSix;
import board.BoardSixThree;
import board.BoardSixTwo;
import communication.Command;
import communication.CommandHandler;
import communication.EndTurnRequest;
import communication.EndTurnResponse;
import communication.GameEndedResponse;
import communication.GameStartedResponse;
import communication.GameplayRequest;
import communication.MoveRequest;
import communication.MoveResponse;
import communication.PossibleMovesRequest;
import communication.PossibleMovesResponse;
import communication.Request;
import communication.Response;
import communication.SomeoneJoinedResponse;
import communication.SomeoneLeftResponse;
import communication.StartFastGameResponse;
import communication.StartFastGameResponse.GameState;
import communication.StartTurnResponse;
import communication.WinResponse;

public class GameHandler implements Runnable {

	private CopyOnWriteArrayList<ClientHandler> clients;
	private BlockingQueue<GameplayRequest> receivedRequests;
	private Thread gameThread;
	private volatile GameInfo gameInfo;
	private Game game;
	private static AtomicInteger gameCounter = new AtomicInteger(0);
	private ClientHandler currentClient;
	int currentPlayerID;
	private boolean turnEnded = false;

	public GameHandler(GameType gameType, ClientHandler creator) {
		clients = new CopyOnWriteArrayList<>();
		receivedRequests = new LinkedBlockingQueue<>();
		gameInfo = new GameInfo(gameCounter.getAndIncrement(), "Game", gameType, GameState.WAITING_FOR_PLAYERS,
				new ArrayList<>());
		clients.add(creator);
		gameInfo.addClientInfo(creator.getClientInfo());
		//Server.getInstance().getServerGUI().addToLog("Utowrzono GameHandler'a");
	}

	void setGameThread(Thread thread) {
		gameThread = thread;
	}
	

	int getConnectedClientsNumber() {
		return clients.size();
	}

	int getExpectedClientsNumber() {
		return gameInfo.getType().getPlayersNumber();
	}

	boolean waitsForPlayers() {
		if (getConnectedClientsNumber() != getExpectedClientsNumber())
			return true;
		return false;
	}

	public void addClient(ClientHandler client) {
		notifyClients(new SomeoneJoinedResponse(client.getClientInfo()));
		clients.add(client);
		client.setGameHandler(this);
		gameInfo.addClientInfo(client.getClientInfo());
		
		if (!waitsForPlayers()) {
			gameInfo.setState(GameState.STERTED);
			gameThread.start();
		}
	}

	boolean gameStarted() {
		if (game != null && game.getState() == Game.GameState.PENDING)
			return true;
		return false;
	}

	public void removeClient(ClientHandler client) {
		int i = clients.indexOf(client);
		clients.remove(client);
		gameInfo.removeClientInfo(client.getClientInfo());
		notifyClients(new SomeoneLeftResponse(client.getClientInfo()));
		if (gameStarted() && !game.getPlayer(i).hasFinished()) {
			notifyClients(new GameEndedResponse());
			Server.getInstance().removeGameHandler(this);
			gameThread.interrupt();
		}
	}

	void notifyClients(Response response) {
		for (ClientHandler c : clients) {
			c.sendResponse(response);
		}
	}

	BlockingQueue<GameplayRequest> getRequestQueue() {
		return receivedRequests;
	}

	GameInfo getGameInfo() {
		return gameInfo;
	}

	void createGame() {
		int playersNum = getExpectedClientsNumber();
		
		if (playersNum == 2) {
			game = new Game(new BoardSixTwo());
		}
		if (playersNum == 3) {
			game = new Game(new BoardSixThree());
		}
		if (playersNum == 4) {
			game = new Game(new BoardSixFour());
		}
		if (playersNum == 6) {
			game = new Game(new BoardSixSix());
		}
		
		for(int i = 0; i < clients.size(); i++) {
			game.addPlayer();
			clients.get(i).getClientInfo().setPlayerID(i);
		}

	}

	void endTurn() {
		turnEnded = true;
		game.endTurn(currentPlayerID);
	}

	boolean isTurnEnded() {
		if (turnEnded) {
			turnEnded = false;
			return true;
		}
		return false;
	}
	
	int chooseFirstPlayerIndex() {
		int randomNum = ThreadLocalRandom.current().nextInt(0, clients.size());
		return randomNum;
	}

	@Override
	public void run() {
		createGame();
		GameplayRequest request;
		GameplayRequestHandler handler = new GameplayRequestHandler();
		notifyClients(new GameStartedResponse(gameInfo.getConnectedClientsInfo()));
		for (int i = chooseFirstPlayerIndex(); game.getState() == Game.GameState.PENDING; i = (++i % clients.size())) {
			if (game.getPlayer(i).hasFinished())
				continue;
			currentClient = clients.get(i);
			currentPlayerID = i;
			notifyClients(new StartTurnResponse(currentClient.getClientInfo()));
			try {
				while (!isTurnEnded() && ((request = receivedRequests.take()) != null) ) {
					request.accept(handler);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				return;
			}
		}
		notifyClients(new GameEndedResponse());
		Server.getInstance().removeGameHandler(this);
	}

	public class GameplayRequestHandler extends CommandHandler {

		public void handle(PossibleMovesRequest request) {
			currentClient.sendResponse(
					new PossibleMovesResponse(game.checkValidMoves(currentPlayerID, request.getPawnPos())));
		}

		public void handle(MoveRequest request) {
			game.move(currentPlayerID, request.getOldPos(), request.getNewPos());
			notifyClients(new MoveResponse(currentClient.getClientInfo(), request.getOldPos(), request.getNewPos()));
			if (game.getPlayer(currentPlayerID).hasFinished()) {
				notifyClients(new WinResponse(currentClient.getClientInfo()));
			}
			else {
				currentClient.sendResponse(new PossibleMovesResponse(game.checkValidMoves(currentPlayerID, request.getNewPos())));
			}
		}

		public void handle(EndTurnRequest request) {
			notifyClients(new EndTurnResponse(currentClient.getClientInfo()));
			endTurn();
		}

		@Override
		public void defaultHandle(Command command) {
			// TODO Auto-generated method stub

		}

	}
}
