import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, RxTwitterClient.class})
public class RxTwitterClientTest {

    private static final Logger logger = LoggerFactory.getLogger(TwitterClientTest.class);

    private static final long USER_ID = 146882655L;

    @Autowired
    private RxTwitterClient rxTwitterClient;

    @Test
    public void testShowUserById() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        rxTwitterClient.showUserById(USER_ID).subscribe(new Observer<User>() {
            @Override
            public void onCompleted() {
                logger.debug("onCompleted");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                logger.error("onError", e);
                latch.countDown();
            }

            @Override
            public void onNext(User user) {
                logger.debug("onNext: {}", user.toString());
            }
        });

        latch.await();
    }

    @Test
    public void testGetFollowersByUserId() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        rxTwitterClient.getFollowersByUserId(USER_ID).subscribe(new Observer<Long>() {
            final List<Long> ids = new ArrayList<>();

            public void onCompleted() {
                logger.debug("XXX onCompleted: got {} ids", ids.size());
                latch.countDown();
            }

            public void onError(Throwable e) {
                logger.error("XXX onError: got {} ids", ids.size(), e);
                latch.countDown();
            }

            public void onNext(Long id) {
                if (ids.contains(id)) {
                    logger.warn("XXX Got duplicate: {}", id);
                    return;
                }

                ids.add(id);

                if (ids.size() % 1000 == 0) {
                    logger.debug("XXX Got {} ids so far...", ids.size());
                }
            }
        });

        logger.debug("XXX Awaiting result...");
        latch.await();
        logger.debug("Done!");
    }

    @Test
    public void testGetFriendsByUserId() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        rxTwitterClient.getFriendsByUserId(1292574098).subscribe(new Observer<Long>() {
            final List<Long> ids = new ArrayList<>();

            public void onCompleted() {
                logger.debug("XXX onCompleted: got {} ids", ids.size());
                logger.debug("XXX onCompleted: ids={}", ids);
                latch.countDown();
            }

            public void onError(Throwable e) {
                logger.error("XXX onError: got {} ids", ids.size(), e);
                latch.countDown();
            }

            public void onNext(Long id) {
                if (ids.contains(id)) {
                    logger.warn("XXX Got duplicate: {}", id);
                    return;
                }

                ids.add(id);

                if (ids.size() % 1000 == 0) {
                    logger.debug("XXX Got {} ids so far...", ids.size());
                }
            }
        });

        logger.debug("XXX Awaiting result...");
        latch.await();
        logger.debug("Done!");
    }

    @Test
    public void testLookupUsersById() throws Exception {
        List<Long> ids = Arrays.asList(
                456723411L,
                3253840887L,
                2829684051L,
                438449380L,
                427596141L,
                2726425412L,
                285532415L,
                282018532L,
                1358542700L,
                250656579L,
                2841144058L,
                2373141377L,
                319103575L,
                624879916L,
                2311756034L,
                31702589L,
                3382008041L,
                3382756786L,
                344673563L,
                52331207L,
                2208593016L,
                320225418L,
                3250934368L,
                3309968080L,
                154530823L,
                381794751L,
                2742078001L,
                306136669L,
                165610510L,
                180505807L,
                354876107L,
                23936177L,
                296456471L,
                2422329662L,
                3026446647L,
                430733591L,
                2940090053L,
                599979926L,
                1011369822L,
                227929901L,
                2445370958L,
                432280376L,
                310079801L,
                254098644L,
                16973333L,
                1294823174L,
                411011256L,
                437489357L,
                2781473942L,
                161199279L,
                1746709694L,
                2608317396L,
                2248365228L,
                50741122L,
                2390974326L,
                20479813L,
                39485393L,
                2318858732L,
                557789557L,
                538021652L,
                471049037L,
                2420953021L,
                173904681L,
                429256025L,
                634725546L,
                46844699L,
                316507065L,
                519685505L,
                158723111L,
                253261225L,
                2448634412L,
                613525195L,
                538432547L,
                2388851832L,
                205459720L,
                2281768368L,
                613304376L,
                2312894523L,
                1111317168L,
                449689677L,
                199811368L,
                1967216306L,
                2149973089L,
                137459628L,
                12191752L,
                103238195L,
                1145902908L,
                442138521L,
                317147065L,
                445608696L,
                17790274L,
                162526847L,
                110513580L,
                230417869L,
                231911634L,
                735278359L,
                475172454L,
                201263457L,
                503707297L,
                1953909074L
        );

        CountDownLatch latch = new CountDownLatch(1);
        List<User> users = new ArrayList<>();
        rxTwitterClient.lookupUsersById(ids).subscribe(new Observer<User>() {
            @Override
            public void onCompleted() {
                logger.debug("XXX onCompleted: got {} users", users.size());
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                logger.debug("XXX onError", e);
                latch.countDown();
            }

            @Override
            public void onNext(User user) {
                logger.debug("XXX onNext: user id={}, screenName={}", user.getId(), user.getScreenName());
                users.add(user);
            }
        });

        logger.debug("XXX Awaiting result...");
        latch.await();
        logger.debug("XXX Done!");
    }

    @Test
    public void testLookupUsersByName() throws Exception {
        List<String> names = Arrays.asList(
                "StalinGulag",
                "wylsacom",
                "navalny",
                "Fake_MIDRF",
                "im_hank_",
                "VRSoloviev",
                "a_malahov",
                "i_korotchenko",
                "poroshenko",
                "Alexey_Pushkov",
                "KermlinRussia",
                "LavrovMuesli",
                "rykov",
                "HistoryFoto",
                "dimsmirnov175",
                "adagamov",
                "DmKiselevTV",
                "A_Gasparyan",
                "Sandy_mustache",
                "gniloywest",
                "anatoliisharii",
                "MedvedevRussia",
                "nstarikov",
                "seksiludi",
                "history_RF",
                "belamova",
                "Shulz",
                "MargoSavazh",
                "Pasha_Mickey",
                "YevhenS",
                "cynicarea",
                "putin_ww",
                "alburov",
                "mc_maxim",
                "PutinRF",
                "Xudozhnikipoeti",
                "McFaul",
                "spacelordrock",
                "ADedurenko",
                "tina_kandelaki",
                "SFGO76",
                "dostoverkin",
                "CrimeaUA1",
                "ErnestMakarenko",
                "EdvardAteva",
                "myrevolutionrus",
                "jetvillains",
                "FakeMORF",
                "varlamov",
                "soulstray",
                "tombreadley",
                "Yagozarussia",
                "zubovnik",
                "Peredorosl",
                "ComradZampolit",
                "MosSobyanin",
                "TukvaSociopat",
                "v1lat",
                "LevSharansky",
                "rasstriga",
                "TarasBerezovets",
                "norimyxxxo",
                "Rogozin",
                "edvlimonov",
                "OlegLurie",
                "Elshad_Babaev",
                "Khinshtein",
                "BuggyBugler",
                "rishikesh_news",
                "SPaWN_ua",
                "korobkov",
                "bokr74",
                "zapvv",
                "KSyomin",
                "pavelsheremet",
                "Sergey_Elkin",
                "twitted_knitter",
                "leonidvolkov",
                "SaakashviliM",
                "M_Simonyan",
                "AndrewLVUA",
                "SPB_citizen",
                "feeling_so_real",
                "iremeslo",
                "cherdantsev",
                "StarshinaZapasa",
                "puppenhausclub",
                "VojnavIstorii",
                "BuzzFeedRU",
                "Yatsenyuk_AP",
                "Realuran",
                "MaksChepay",
                "Pravdiva_pravda",
                "galyonkin",
                "gudkovd",
                "Zhirinovskiy",
                "JuMistress",
                "VladimirMarkin",
                "sranysovok",
                "nightseparator"
        );

        CountDownLatch latch = new CountDownLatch(1);
        List<User> users = new ArrayList<>();
        rxTwitterClient.lookupUsersByName(names).subscribe(new Observer<User>() {
            @Override
            public void onCompleted() {
                logger.debug("XXX onCompleted: got {} users", users.size());
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                logger.debug("XXX onError", e);
                latch.countDown();
            }

            @Override
            public void onNext(User user) {
                logger.debug("XXX onNext: user id={}, screenName={}", user.getId(), user.getScreenName());
                users.add(user);
            }
        });

        logger.debug("XXX Awaiting result...");
        latch.await();
        logger.debug("XXX Done!");
    }
}