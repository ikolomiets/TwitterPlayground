import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, RxTwitterClient.class, Neo4jConfig.class})
public class Neo4jClientInActionTest {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jClientInActionTest.class);

    @Autowired
    private RxTwitterClient rxTwitterClient;

    @Autowired
    private Neo4jClient neo4jClient;

    @Test
    public void testAction() throws Exception {

        // 153812887,MedvedevRussia,4269782,63
        // 114557496,urgantcom,4009472,3,false
        // 196679942,RealVolya,3531469,91,false
        // 127680760,VeraBrezhneva,3147014,55,false
        // 161199279,mnzadornov,2345773,12,false
        // 65442728,tina_kandelaki,1936187,630,false
        // 81063904,LeraTV,1735651,170,false
        // 154645237,VictoriaBonya,1564549,96,false
        // ****
        // 146882655,VRSoloviev,1181490,290

        User user = new User();
        user.setId(154645237);
        user.setScreenName("VictoriaBonya");

        neo4jClient.mergeUser(user);

        CountDownLatch latch = new CountDownLatch(2);

        rxTwitterClient.getFollowersByUserId(user.getId()).buffer(200).subscribe(new Observer<List<Long>>() {
            @Override
            public void onCompleted() {
                logger.debug("getFollowersByUserId onCompleted");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                logger.error("getFollowersByUserId onError", e);
                latch.countDown();
            }

            @Override
            public void onNext(List<Long> followerIds) {
                neo4jClient.createFollowerRelations(followerIds, user);
            }
        });

        rxTwitterClient.getFriendsByUserId(user.getId()).subscribe(new Observer<Long>() {
            @Override
            public void onCompleted() {
                logger.debug("getFriendsByUserId onCompleted");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                logger.error("getFriendsByUserId onError", e);
                latch.countDown();
            }

            @Override
            public void onNext(Long userId) {
                User friend = new User();
                friend.setId(userId);

                neo4jClient.createRelation(user, friend);
            }
        });

        latch.await();
    }

    @Test
    public void testAction2() throws Exception {
        Set<String> blacklist = new HashSet<>(Arrays.asList(
                "poroshenko",
                "VOGUERussia",
                "dumagovru",
                "LoveRadioRu",
                "MchsRussia",
                "GLAMOUR_Russia",
                "mefimus",
                "natomission_ru",
                "Gorod_312",
                "Yatsenyuk_AP",
                "hpeterdik",
                "memdmitri",
                "usynlihe03",
                "atwinBona",
                "tvoyplanetary",
                "AdWordsRussia",
                "marevumeno",
                "5channel",
                "bocharik",
                "Vitaliy_Klychko",
                "AvakovArsen",
                "dethArrud",
                "trubetskoyband",
                "DmitryTymchuk",
                "skrypin",
                "Korrespondent",
                "OVLiashko",
                "LesyaOrobets",
                "tdanylenko",
                "EchoMskNews",
                "kievtypical",
                "AnPrikhodko",
                "Burberry_Russia",
                "agrytsenko",
                "okeanelzy",
                "rus_fas",
                "portnikov",
                "Turchynov",
                "HistoryFoto",
                "MMOMA",
                "Leshchenkos",
                "miloserdie_ru",
                "villagemsk",
                "AikynT",
                "airastana",
                "ComedyRu",
                "v1lat",
                "AShevch",
                "ach_gov_ru",
                "appleip3",
                "SaakashviliM",
                "lenitsky",
                "Fake_MIDRF",
                "PavloKlimkin",
                "KhabarTV",
                "8ulgakov",
                "khl",
                "AloudRU",
                "unian",
                "shaxzoda",
                "rsgov",
                "hcSKA",
                "Pelmeny",
                "skolkovo",
                "rutv",
                "StankoNastya",
                "dw_russian",
                "belamova",
                "bokr74",
                "LYuldasheva",
                "Genproc",
                "GanievaRayhon",
                "sosmosmetro",
                "STsegolko",
                "AluaKonarova",
                "GoodGoodMag",
                "aifonline",
                "nigelgod",
                "championat",
                "Karandash_pro",
                "ADeshchytsia",
                "russia_trend",
                "MGongadze",
                "myrevolutionrus",
                "sranysovok",
                "SammmyButler",
                "ProjectorPH",
                "sportexpress",
                "chelobl",
                "katushacycling",
                "aronets",
                "interfaxua",
                "Yannis Philippakis",
                "BrainStorm_LV",
                "anti_maydan",
                "im_hank_",
                "Automaidan",
                "vcru",
                "kashasaltsova",
                "obozrevatel_ua",
                "BobbyNewberry",
                "AbramRomovich",
                "tombreadley",
                "RFI_Rus",
                "mailru",
                "mdobkin",
                "dimychman",
                "mvd_official",
                "Bobina",
                "itsector",
                "vo_svoboda",
                "yelikbayev",
                "azakuskin",
                "IgorZ_ua",
                "Sloviansk",
                "BlockPoroshenka",
                "NOVORUSSIA2015",
                "SPaWN_ua",
                "twitted_knitter",
                "strelkov_info",
                "bnewskz",
                "ZaxidNet",
                "gazgolder",
                "AlcoHistory",
                "vezhlivo",
                "YevhenS",
                "Alekzander_Luk",
                "Gerashchenko7",
                "MaxBarskih",
                "Uyanga_ts",
                "vtemeofficial",
                "zn_ua",
                "zanoza_kg",
                "Pipsec",
                "yandexmusic",
                "GazetaRu_All",
                "tvrain_live",
                "WhiteRaSC",
                "teamnavalny",
                "Hy_Donetsk",
                "seksiludi",
                "VolodymyrAriev",
                "1DirectionFM",
                "112by",
                "FakeMORF",
                "ftbl60",
                "ARMY_SOS",
                "Xudozhnikipoeti",
                "AlexYanovsky",
                "mc_maxim",
                "villagespb",
                "yandexmaps",
                "fake_kiselev",
                "Shulz",
                "superumka",
                "doproverki_ru",
                "Polk_Azov",
                "Pravdiva_pravda",
                "MagnaRussia",
                "SonyPicturesRU",
                "golosinfo",
                "Favstar_Bot",
                "narodprotiv",
                "GazetaRu_Lenta",
                "taygainfo",
                "Motolko",
                "yandexsupport",
                "SerebroOfficial",
                "KlichkoVitalii",
                "365advices",
                "ianekdot",
                "SputnikATO",
                "adidasRU",
                "GazetaRu_Cult",
                "theinsider_ua",
                "bryhynets",
                "obrnadzor",
                "nnr_news",
                "LeandroAlmeida4",
                "Vederkina",
                "wings_phoenix",
                "Realuran",
                "alfa_bank",
                "op_rf",
                "IntelRussia",
                "great_ukr",
                "roscomnadzor",
                "YodNews",
                "fadm_ru",
                "belpartisan",
                "e_styd",
                "krymrealii",
                "pr0stitutka",
                "minsvyaz_news",
                "GazetaRu_Auto",
                "UKRINFORM",
                "jacobyononsen",
                "openrussia_org",
                "tweetsNV",
                "TheArtsmuseum",
                "ya_events",
                "World_Oil_Price",
                "sibfm",
                "history_RF",
                "KremlinMuseums",
                "feels_like_1984",
                "GazetaRu_Sport",
                "vida_ying_yang",
                "Gazeta_Politics",
                "runews",
                "kp_star",
                "johnnytwett",
                "anewsfeed",
                "kpby",
                "putin2012",
                "albus1881",
                "minpromtorg_rus",
                "NewsRuNovosti",
                "rosnano_ru",
                "kozakmamai",
                "Novorussia_ru",
                "GazetaRu_Biz",
                "TarasBerezovets",
                "zvezdanews",
                "CrimeaUA1",
                "vrachiru",
                "mobilreview",
                "olarhat",
                "IgromaniaRu",
                "MashaAlekhina",
                "gniloywest",
                "AndrewLVUA",
                "RussiansForward",
                "KapitanKenguru",
                "proyasnil",
                "CatDemokrat",
                "big_b_brother",
                "demalliance",
                "19012014ps",
                "Culture_RF",
                "StanShaw1",
                "Yuriy_Sergeyev",
                "0629ComUa",
                "meteoinfo_by",
                "LUGANSK_TODAY",
                "galyonkin",
                "kprf",
                "guberman_igor",
                "ProtectUkraine",
                "Krasoti_Rossii",
                "Ukatayka777",
                "kanobu_ru",
                "AndroidDigestRu",
                "pravoslavie_rf",
                "PeterShuklinov",
                "TsogtgerelB",
                "_Shantee_",
                "ru_maximonline",
                "fuckysebastopol",
                "mos_trans",
                "24tvua",
                "Mir24TV",
                "Sam_Nickel",
                "sodel_vlad",
                "SonyXperiaRu",
                "dmitry_gordon",
                "rescuero",
                "feeling_so_real",
                "svo_airport",
                "pravo_ru",
                "US_progress",
                "VVP2_0",
                "MalishevaE",
                "HuSnizhne",
                "zhidkovskiy",
                "vmirechudes",
                "bookierating",
                "vsezaldpr",
                "LeadAirship",
                "saved_jpg",
                "GazetaRu_Social",
                "ANAKOYHER",
                "VojnavIstorii",
                "modavmetro",
                "cynicarea",
                "RussianRUB",
                "hu_lviv",
                "RosUznik",
                "belta_news",
                "vesti_kpss",
                "COV_Novorossia",
                "EgoRZemtsoV",
                "MinskMetroNet",
                "Sapelkin",
                "360tv",
                "aivaras_aivaras",
                "Cinemaholics_",
                "4dk_consultant",
                "svpressa",
                "FakeNTV",
                "BundesligaRus",
                "KinoMacho",
                "newsua",
                "PoznerLife",
                "Atl_Aztecs",
                "vilnezheettya",
                "Turar111",
                "n_jaresko",
                "lyapotasasha",
                "RS_Ukraine",
                "avramchuk_katya",
                "aliev_aliev",
                "banderenko",
                "Ogrysko",
                "AMykhailova",
                "AfricanoBOi",
                "sergonaumovich",
                "rusplt",
                "kanalukraine",
                "afishavozduh",
                "ProkyrorCrimea",
                "bloodypastor",
                "Fontanka_spb",
                "FiztehRadio",
                "kalynych25",
                "SobiNews_com",
                "yapomoshnik",
                "rvio_ru",
                "RenTV",
                "donikroman",
                "pharaon01",
                "svabodka",
                "vseshutochki",
                "dem_coalition",
                "focusua",
                "DmKiselevTV",
                "radiobabay",
                "nakemon2",
                "rishikesh_news",
                "hui_tam_pel",
                "AnaRybachenko",
                "PolitYumor",
                "mara_beyka",
                "minstroyRF",
                "barak_obmana",
                "LifehackerRu",
                "pqorama",
                "Mayskolpino",
                "freedom_in_ua",
                "krim_v_jope",
                "Weekend_OS",
                "Pravmir",
                "hautetime",
                "Antimaidan_Rf",
                "maksm",
                "mvideo",
                "666_mancer",
                "mediazzzona",
                "nugapacu",
                "FURFURMAG",
                "mtrrrpl",
                "alexmazuka",
                "LentaNovosti",
                "SpbPiter",
                "afishagorod",
                "Doctor_Stefan",
                "emoskva",
                "yaplakalcom",
                "Crimea_Ukr",
                "maryel2002",
                "nomoreanry",
                "TURepublic",
                "LXXSXXHXKKK",
                "fc_yenisey",
                "cossa_ru",
                "dobry_chlopec",
                "MedvedOfRussia",
                "radio24_ua",
                "ea_russia",
                "WilloftheNation",
                "akipress",
                "persident_urk",
                "myLegnica",
                "sar_life",
                "kokskvas",
                "Yoghikitt",
                "sro4no",
                "thqstn",
                "RRED_AARMY",
                "govoritmsk",
                "arturclancy",
                "DNikitich",
                "Daily_minsk",
                "Gladiators_Firm",
                "MotorRu",
                "gusenica_lo",
                "podarizhizn",
                "TipsiTip",
                "blisch",
                "RussiaUN",
                "ferraru",
                "Hu_0dessa",
                "Sasdasdlkasldjkasdlkaj this is the end dlkqwlekqwlejqwlejkqlkzxlkjcl"
                ));

        List<User> users = getToptwitUsers();
        for (User user : users.subList(100, 200)) {
            if (blacklist.contains(user.getScreenName())) {
                logger.warn("Ignore blacklisted user {}", user.getScreenName());
                continue;
            }

            User existingUser = neo4jClient.getUserById(user.getId());
            if (existingUser != null && existingUser.getScreenName() != null) {
                logger.debug("Skip existing user id={}, screenName={}", user.getId(), user.getScreenName());
                continue;
            }

            logger.debug("Fetching relations for user id={}, screenName={}", user.getId(), user.getScreenName());

            neo4jClient.mergeUser(user);

            CountDownLatch latch = new CountDownLatch(2);

            rxTwitterClient.getFollowersByUserId(user.getId()).buffer(200).subscribe(new Observer<List<Long>>() {
                @Override
                public void onCompleted() {
                    logger.debug("getFollowersByUserId onCompleted for user id={}, screenName={}", user.getId(), user.getScreenName());
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    logger.error("getFollowersByUserId onError for user id={}, screenName={}", user.getId(), user.getScreenName(), e);
                    latch.countDown();
                }

                @Override
                public void onNext(List<Long> followerIds) {
                    neo4jClient.createFollowerRelations(followerIds, user);
                }
            });

            rxTwitterClient.getFriendsByUserId(user.getId()).subscribe(new Observer<Long>() {
                @Override
                public void onCompleted() {
                    logger.debug("getFriendsByUserId onCompleted for user id={}, screenName={}", user.getId(), user.getScreenName());
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    logger.error("getFriendsByUserId onError for user id={}, screenName={}", user.getId(), user.getScreenName(), e);
                    latch.countDown();
                }

                @Override
                public void onNext(Long userId) {
                    User friend = new User();
                    friend.setId(userId);

                    neo4jClient.createRelation(user, friend);
                }
            });

            latch.await();
        }
    }

    private List<User> getToptwitUsers() throws IOException {
        List<User> users = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("logs/toptwit_data_sorted_by_followers_3.log"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");

            User user = new User();
            user.setId(Long.parseLong(parts[0]));
            user.setScreenName(parts[1]);

            users.add(user);
        }
        br.close();

        return users;
    }

}