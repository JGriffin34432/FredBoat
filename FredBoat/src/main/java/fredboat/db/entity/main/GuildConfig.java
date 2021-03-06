/*
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package fredboat.db.entity.main;

import fredboat.FredBoat;
import fredboat.db.DatabaseNotReadyException;
import fredboat.db.entity.IEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.sqlsauce.DatabaseException;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "guild_config")
@Cacheable
@Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="guild_config")
public class GuildConfig implements IEntity, Serializable {

    private static final Logger log = LoggerFactory.getLogger(GuildConfig.class);

    private static final long serialVersionUID = 5055243002380106205L;

    @Id
    @Column(name = "guildid", nullable = false)
    private String guildId;

    @Column(name = "track_announce", nullable = false)
    private boolean trackAnnounce = false;

    @Column(name = "auto_resume", nullable = false)
    private boolean autoResume = false;

    @Column(name = "lang", nullable = false)
    private String lang = "en_US";

    //may be null to indicate that there is no custom prefix for this guild
    @Column(name = "prefix", nullable = true, columnDefinition = "text")
    private String prefix;

    public GuildConfig(String id) {
        this.guildId = id;
    }

    @Override
    public void setId(String id) {
        this.guildId = id;
    }

    public GuildConfig() {
    }

    public String getGuildId() {
        return guildId;
    }

    public boolean isTrackAnnounce() {
        return trackAnnounce;
    }

    public void setTrackAnnounce(boolean trackAnnounce) {
        this.trackAnnounce = trackAnnounce;
    }

    public boolean isAutoResume() {
        return autoResume;
    }

    public void setAutoResume(boolean autoplay) {
        this.autoResume = autoplay;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Nullable
    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(@Nullable String prefix) {
        this.prefix = prefix;
    }

    /*@OneToMany
    @JoinColumn(name = "guildconfig")
    private Set<TCConfig> textChannels;

    public GuildConfig(Guild guild) {
        this.guildId = Long.parseLong(guild.getId());

        textChannels = new CopyOnWriteArraySet<>();

        for (TextChannel tc : guild.getTextChannels()) {
            TCConfig tcc = new TCConfig(this, tc);
            textChannels.add(tcc);
        }

        for (TCConfig tcc : textChannels) {
            DatabaseManager.getEntityManager().persist(tcc);
        }
    }

    public Set<TCConfig> getTextChannels() {
        return textChannels;
    }

    public void addTextChannel(TCConfig tcc){
        textChannels.add(tcc);
    }

    public void removeTextChannel(TCConfig tcc){
        textChannels.remove(tcc);
    }

    public long getGuildId() {
        return guildId;
    }
    */

    //shortcut to load the prefix without fetching the whole entity because the prefix will be needed rather often
    // without the rest of the guildconfig information
    @Nullable
    public static Optional<String> getPrefix(long guildId) {
        log.debug("loading prefix for guild {}", guildId);
        //language=JPAQL
        String query = "SELECT gf.prefix FROM GuildConfig gf WHERE gf.guildId = :guildId";
        EntityManager em = null;
        try {
            em = FredBoat.getMainDbConnection().getEntityManager();
            em.getTransaction().begin();
            List<String> result = em.createQuery(query, String.class)
                    .setParameter("guildId", Long.toString(guildId))
                    .getResultList();
            em.getTransaction().commit();
            if (result.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(result.get(0));
            }
        } catch (DatabaseException | PersistenceException e) {
            log.error("Failed to load prefix for guild {}", guildId, e);
            throw new DatabaseNotReadyException(e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
