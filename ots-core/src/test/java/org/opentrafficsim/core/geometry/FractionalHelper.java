package org.opentrafficsim.core.geometry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.core.geometry.OtsLine2d.FractionalFallback;

/**
 * Test/development code for the fractional helper stuff.
 */
public final class FractionalHelper
{
    /** Utility class. */
    private FractionalHelper()
    {
        // Do not instantiate
    }

    /**
     * Test/development code for the fractional helper stuff.
     * @param args String[]; the command line arguments (not used)
     * @throws OtsGeometryException in case of error
     * @throws SecurityException ...
     * @throws NoSuchFieldException ...
     * @throws IllegalAccessException ...
     * @throws IllegalArgumentException ...
     */
    public static void main(final String[] args) throws OtsGeometryException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException
    {
        /*-
        OtsLine2d line = new OtsLine2d(new Point2d(-263.811, -86.551, 1.180), new Point2d(-262.945, -84.450, 1.180),
                new Point2d(-261.966, -82.074, 1.180), new Point2d(-260.890, -79.464, 1.198),
                new Point2d(-259.909, -76.955, 1.198), new Point2d(-258.911, -74.400, 1.198),
                new Point2d(-257.830, -71.633, 1.234));
        System.out.println(line.toExcel());
        double[] relativeFractions = new double[] { 0.0, 0.19827228089475762, 0.30549496392494213, 0.5824753163948581,
                0.6815307752261827, 0.7903990449840241, 0.8942375145295614, 1.0 };
        double[] offsets = new double[] { 2.9779999256134, 4.6029999256134, 3.886839156071996, 2.3664845198627207,
                1.7858981925396709, 1.472348149010167, 2.0416709053157285, 2.798692100483229 };
        System.out.println(line.offsetLine(relativeFractions, offsets).toExcel());         
        */

        List<Point2d> list = new ArrayList<>();
        boolean laneOn933 = true;
        if (!laneOn933)
        {
            double x = 0;
            double y = 0;
            double dx = 0.000001;
            double dy = 0.05;
            double ddx = 1.5;
            for (int i = 0; i < 32; i++)
            {
                list.add(new Point2d(x, y));
                x += dx;
                dx *= ddx;
                y += dy;
            }
        }
        else
        {
            String lineStr = "@0   426333.939, 4581372.345@" + "1   426333.92109750526, 4581372.491581111@"
                    + "2   426333.9016207722, 4581372.6364820665@" + "3   426333.8806181711, 4581372.7797264075@"
                    + "4   426333.8581377007, 4581372.921337651@" + "5   426333.8342269785, 4581373.061339286@"
                    + "6   426333.80893323367, 4581373.199754763@" + "7   426333.78230329906, 4581373.336607476@"
                    + "8   426333.75438360614, 4581373.471920755@" + "9   426333.7252201801, 4581373.605717849@"
                    + "10  426333.69485863775, 4581373.738021923@" + "11  426333.6633441839, 4581373.868856039@"
                    + "12  426333.6307216125, 4581373.998243135@" + "13  426333.5970353065, 4581374.1262060385@"
                    + "14  426333.56232923956, 4581374.252767426@" + "15  426333.54571270826, 4581374.331102062@"
                    + "16  426333.53121128445, 4581374.399777128@" + "17  426333.51761287224, 4581374.46141805@"
                    + "18  426333.5035609495, 4581374.524905452@" + "19  426333.4885681211, 4581374.590110448@"
                    + "20  426333.4750534529, 4581374.648530791@" + "21  426333.4586325006, 4581374.71720738@"
                    + "22  426333.44573716016, 4581374.770680802@" + "23  426333.4278589452, 4581374.84273674@"
                    + "24  426333.41565935884, 4581374.891382747@" + "25  426333.39629928104, 4581374.966726161@"
                    + "26  426333.3640042249, 4581375.089202983@" + "27  426333.3310233974, 4581375.210194213@"
                    + "28  426333.2974053264, 4581375.329726505@" + "29  426333.26319745823, 4581375.44782613@"
                    + "30  426333.2284461768, 4581375.564518943@" + "31  426333.1931968143, 4581375.679830365@"
                    + "32  426333.15749366966, 4581375.793785359@" + "33  426333.12138002727, 4581375.9064084105@"
                    + "34  426333.0848981781, 4581376.017723508@" + "35  426333.0526068902, 4581376.127395174@"
                    + "36  426333.0222216131, 4581376.235573194@" + "37  426333.00835773064, 4581376.284013769@"
                    + "38  426332.9916265083, 4581376.342442355@" + "39  426332.9771780217, 4581376.392075247@"
                    + "40  426332.96085931134, 4581376.448026933@" + "41  426332.9448449097, 4581376.5021694945@"
                    + "42  426332.9299564511, 4581376.552350422@" + "43  426332.9123899684, 4581376.610862428@"
                    + "44  426332.87985284685, 4581376.718179138@" + "45  426332.8472718188, 4581376.824143872@"
                    + "46  426332.81468381727, 4581376.92878003@" + "47  426332.78212446393, 4581377.032110168@"
                    + "48  426332.7496281178, 4581377.134155947@" + "49  426332.71722788643, 4581377.234938197@"
                    + "50  426332.68495568086, 4581377.3344768565@" + "51  426332.6528422234, 4581377.432791035@"
                    + "52  426332.6209170973, 4581377.529898969@" + "53  426332.59026768577, 4581377.622609458@"
                    + "54  426332.5618311538, 4581377.708242513@" + "55  426332.5292456913, 4581377.813700842@"
                    + "56  426332.5007497582, 4581377.905735847@" + "57  426332.4725916431, 4581377.996633883@"
                    + "58  426332.4447947076, 4581378.086409748@" + "59  426332.41739884845, 4581378.175020202@"
                    + "60  426332.3904224847, 4581378.262486783@" + "61  426332.37513187295, 4581378.312218361@"
                    + "62  426332.3474726438, 4581378.402429141@" + "63  426332.3203478011, 4581378.491354613@"
                    + "64  426332.2937555201, 4581378.579078223@" + "65  426332.26771504263, 4581378.665610338@"
                    + "66  426332.24224462465, 4581378.750960108@" + "67  426332.21736132156, 4581378.835136287@"
                    + "68  426332.1930813682, 4581378.918146061@" + "69  426332.1694196611, 4581378.999996922@"
                    + "70  426332.1468078785, 4581379.079234334@" + "71  426332.1253935003, 4581379.155326921@"
                    + "72  426332.10456227185, 4581379.230438552@" + "73  426332.08413377195, 4581379.301777359@"
                    + "74  426332.0575671712, 4581379.393246921@" + "75  426332.037751917, 4581379.463051603@"
                    + "76  426332.01541074895, 4581379.543672992@" + "77  426331.9954696024, 4581379.617241848@"
                    + "78  426331.9764488572, 4581379.689794578@" + "79  426331.9581173997, 4581379.761214821@"
                    + "80  426331.9407607595, 4581379.831643043@" + "81  426331.92459788476, 4581379.898797621@"
                    + "82  426331.89349001576, 4581380.036207511@" + "83  426331.8662295119, 4581380.167554456@"
                    + "84  426331.84239882755, 4581380.294825263@" + "85  426331.8220095046, 4581380.41813201@"
                    + "86  426331.80506772455, 4581380.537631294@" + "87  426331.79158302536, 4581380.653536015@"
                    + "88  426331.78158027114, 4581380.766126917@" + "89  426331.7754554946, 4581380.838605414@"
                    + "90  426331.76793314604, 4581380.909291444@" + "91  426331.7605002508, 4581381.016285149@"
                    + "92  426331.75725734304, 4581381.119549306@" + "93  426331.75814653496, 4581381.219559045@"
                    + "94  426331.76316353114, 4581381.316908372@" + "95  426331.7723867522, 4581381.412305131@"
                    + "96  426331.7860053539, 4581381.506554079@" + "97  426331.80434182915, 4581381.600527881@"
                    + "98  426331.82733581704, 4581381.692992337@" + "99  426331.8531803791, 4581381.777938947@"
                    + "100 426331.884024255, 4581381.864352291@" + "101 426331.92063241004, 4581381.953224321@"
                    + "102 426331.96390912175, 4581382.045434713@" + "103 426331.9901409878, 4581382.095566823@"
                    + "104 426332.0148562894, 4581382.141714169@" + "105 426332.05172826024, 4581382.204388889@"
                    + "106 426332.12722889386, 4581382.323121141@" + "107 426332.1628785428, 4581382.375872464@"
                    + "108 426332.22007742553, 4581382.462661629@" + "109 426332.26023980865, 4581382.523784153@"
                    + "110 426332.3033344728, 4581382.586422447@" + "111 426332.34946240357, 4581382.650580184@"
                    + "112 426332.3987196004, 4581382.716255575@" + "113 426332.4511967281, 4581382.783441929@"
                    + "114 426332.50697922776, 4581382.852128648@" + "115 426332.56614731904, 4581382.922301916@"
                    + "116 426332.628776037, 4581382.993945288@" + "117 426332.6949354622, 4581383.067040358@"
                    + "118 426332.76469110255, 4581383.141567508@" + "119 426332.8381037568, 4581383.217505949@"
                    + "120 426332.91523022414, 4581383.294834619@" + "121 426332.9961233405, 4581383.373532268@"
                    + "122 426333.0808322224, 4581383.453577724@" + "123 426333.1693585424, 4581383.534909724@"
                    + "124 426333.26164044754, 4581383.61741792@" + "125 426333.3650128907, 4581383.707446191@";
            int fromIndex = 0;
            while (true)
            {
                int at1 = lineStr.indexOf('@', fromIndex);
                fromIndex = at1 + 1;
                int at2 = lineStr.indexOf('@', fromIndex);
                if (at2 < 0)
                {
                    break;
                }
                fromIndex = at2;

                String subStr = lineStr.substring(at1 + 5, at2);
                int comma = subStr.indexOf(',');
                double x = Double.valueOf(subStr.substring(0, comma));
                double y = Double.valueOf(subStr.substring(comma + 1));

                list.add(new Point2d(x, y));

            }
        }
        OtsLine2d line = new OtsLine2d(list);

        line.projectFractional(null, null, 1.0, 0.5, FractionalFallback.NaN); // creates fractional helper points

        // create line of fractional helper points, give NaN points for null values
        Point2d[] array = getFractionalHelperCenters(line);
        for (int i = 0; i < array.length; i++)
        {
            if (array[i] == null)
            {
                array[i] = new Point2d(Double.NaN, Double.NaN);
            }
        }
        OtsLine2d helpers = new OtsLine2d(getFractionalHelperCenters(line));

        // create Matlab compatible strings of lines
        StringBuilder str = new StringBuilder();
        str.append("line = [");
        String sep = "";
        for (Point2d p : line.getPoints())
        {
            str.append(String.format(Locale.US, "%s %.8f, %.8f", sep, p.x, p.y));
            sep = ",";
        }
        str.append("];\n");

        str.append("helpers = [");
        sep = "";
        for (Point2d p : helpers.getPoints())
        {
            str.append(String.format(Locale.US, "%s %.8f, %.8f", sep, p.x, p.y));
            sep = ",";
        }
        str.append("];\n");

        System.out.print(str);
    }

    /**
     * Dirty hack.
     * @param line OtsLine2d; the line
     * @return OTSPoine3D[]
     * @throws IllegalArgumentException ...
     * @throws IllegalAccessException ...
     * @throws NoSuchFieldException ...
     * @throws SecurityException ...
     */
    static Point2d[] getFractionalHelperCenters(final OtsLine2d line)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
    {
        Field fhcArray = OtsLine2d.class.getDeclaredField("fractionalHelperCenters");
        fhcArray.setAccessible(true);
        return (Point2d[]) fhcArray.get(line);
    }

}
