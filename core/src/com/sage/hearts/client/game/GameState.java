package com.sage.hearts.client.game;

import com.sage.hearts.client.HeartsGame;
import com.sage.hearts.client.network.Client;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.server.network.ServerCode;
import com.sage.hearts.server.network.ServerPacket;
import com.sage.hearts.utils.renderable.RenderableCard;
import com.sage.hearts.utils.renderable.RenderableCardList;
import com.sage.hearts.utils.renderable.RenderableHand;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameState {
    private Updater updater = new Updater();

    private final HeartsGame game;

    public final RenderablePlayer[] players = new RenderablePlayer[4];

    public RenderablePlayer turnPlayer;
    public RenderablePlayer winningPlayer;

    public RenderablePlayer thisPlayer;
    public final RenderableHand<RenderableHeartsCard> thisPlayerHand = new RenderableHand<>();

    public ServerCode lastServerCode;

    public String message = "";

    public boolean heartsBroke = false;

    public GameState(HeartsGame game) {
        this.game = game;
    }

    public boolean update(Client client) {
        boolean didUpdate = false;
        Optional<ServerPacket> p;
        while((p = client.getPacket()).isPresent()) {
            applyUpdate(p.get());
            didUpdate = true;
        }
        return didUpdate;
    }

    private void applyUpdate(ServerPacket updatePacket) {
        updater.update(updatePacket.networkCode, updatePacket.data);
    }

    private class Updater {
        private void update(ServerCode serverCode, Map data) {
            lastServerCode = serverCode;
        }
    }

    public class Actions {
        public void sendWarheads(Client client) {
            RenderableCardList<RenderableHeartsCard> selectedCards = thisPlayerHand.stream()
                    .filter(RenderableCard::isSelected)
                    .collect(Collectors.toCollection(RenderableCardList::new));
            if(selectedCards.size() == 3) {
                ClientPacket packet = new ClientPacket();
                packet.data.put("warheads", selectedCards.toCardNumList());
                try {
                    client.sendPacket(packet);
                } catch(IOException e) {
                    message = "There was an error while trying to contact the server... try again";
                    return;
                }
                thisPlayerHand.removeAll(selectedCards);
                message = "Sending warheads...";
            } else {
                message = "You must select 3 cards to send";
            }
        }
    }
}
