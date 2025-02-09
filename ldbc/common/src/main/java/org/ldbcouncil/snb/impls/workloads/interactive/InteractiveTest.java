package org.ldbcouncil.snb.impls.workloads.interactive;

import com.google.common.collect.ImmutableList;
import org.ldbcouncil.snb.driver.Db;
import org.ldbcouncil.snb.driver.workloads.interactive.LdbcSnbInteractiveWorkload;
import org.ldbcouncil.snb.driver.workloads.interactive.queries.*;
import org.ldbcouncil.snb.impls.workloads.SnbTest;
import org.junit.Test;

import java.util.Date;

public abstract class InteractiveTest<D extends Db> extends SnbTest<D>
{

    public InteractiveTest( D db )
    {
        super( db, new LdbcSnbInteractiveWorkload() );
    }

    @Test
    public void testQuery1() throws Exception
    {
        run( db, new LdbcQuery1( 14L, "Jan", LIMIT ) );
    }

    @Test
    public void testQuery2() throws Exception
    {
        run( db, new LdbcQuery2( 14L, new Date( 1354060800000L ), LIMIT ) );
    }

    @Test
    public void testQuery3() throws Exception
    {
        run( db, new LdbcQuery3a( 14L, "Hungary", "Panama", new Date( 1334448000000L ), 180, LIMIT ) );
    }

    @Test
    public void testQuery4() throws Exception
    {
        run( db, new LdbcQuery4( 14L, new Date( 1347577200000L ), 182, LIMIT ) );
    }

    @Test
    public void testQuery5() throws Exception
    {
        run( db, new LdbcQuery5( 14L, new Date( 1325376000000L ), LIMIT ) );
    }

    @Test
    public void testQuery6() throws Exception
    {
        run( db, new LdbcQuery6(14L, "Friedrich_Schiller", LIMIT ) );
    }

    @Test
    public void testQuery7() throws Exception
    {
        run( db, new LdbcQuery7( 14L, LIMIT ) );
    }

    @Test
    public void testQuery8() throws Exception
    {
        run( db, new LdbcQuery8( 14L, LIMIT ) );
    }

    @Test
    public void testQuery9() throws Exception
    {
        run( db, new LdbcQuery9( 14L, new Date( 1346112000000L ), LIMIT ) );
    }

    @Test
    public void testQuery10() throws Exception
    {
        run( db, new LdbcQuery10( 14L, 7, LIMIT ) );
    }

    @Test
    public void testQuery11() throws Exception
    {
        run( db, new LdbcQuery11( 14L, "Puerto_Rico", 2004, LIMIT ) );
    }

    @Test
    public void testQuery12() throws Exception
    {
        run( db, new LdbcQuery12( 14L, "BasketballPlayer", LIMIT ) );
    }

    @Test
    public void testQuery13() throws Exception
    {
        run( db, new LdbcQuery13a( 14L, 26388279067108L ) );
    }

    @Test
    public void testQuery14() throws Exception
    {
        run( db, new LdbcQuery14a( 14L, 2199023256862L ) );
    }

    @Test
    public void testShortQuery1() throws Exception
    {
        run( db, new LdbcShortQuery1PersonProfile( 14L ) );
    }

    @Test
    public void testShortQuery2() throws Exception
    {
        run( db, new LdbcShortQuery2PersonPosts( 14L, LIMIT ) );
    }

    @Test
    public void testShortQuery3() throws Exception
    {
        run( db, new LdbcShortQuery3PersonFriends( 14L ) );
    }

    @Test
    public void testShortQuery4() throws Exception
    {
        run( db, new LdbcShortQuery4MessageContent( 2061584476422L ) );
    }

    @Test
    public void testShortQuery5() throws Exception
    {
        run( db, new LdbcShortQuery5MessageCreator( 2061584476422L ) );
    }

    @Test
    public void testShortQuery6() throws Exception
    {
        run( db, new LdbcShortQuery6MessageForum( 2061584476422L ) );
    }

    @Test
    public void testShortQuery7() throws Exception
    {
        run( db, new LdbcShortQuery7MessageReplies( 2061584476422L ) );
    }

    @Test
    public void testUpdateQuery1() throws Exception
    {
        final LdbcInsert1AddPerson.Organization university1 = new LdbcInsert1AddPerson.Organization( 5142L, 2004 );
        run( db, new LdbcInsert1AddPerson(
                     10995116277777L,
                     "Almira",
                     "Patras",
                     "female",
                     new Date( 425606400000L ), // note that java.util.Date has no timezone
                     new Date( 1291394394934L ),
                     "193.104.227.215",
                     "Internet Explorer",
                     246L,
                     ImmutableList.of( "ru", "en" ),
                     ImmutableList.of( "Almira10995116277777@gmail.com", "Almira10995116277777@gmx.com" ),
                     ImmutableList.of( 1916L ),
                     ImmutableList.of( university1 ),
                     ImmutableList.of()
             )
        );

        run( db, new LdbcDelete1RemovePerson( 10995116277777L ) );
    }

    @Test
    public void testUpdateQuery2() throws Exception
    {
        run( db, new LdbcInsert2AddPostLike( 8796093022239L, 206158430617L, new Date( 1290749436322L ) ) );
        // add delete here
    }

    @Test
    public void testUpdateQuery3() throws Exception
    {
        run( db, new LdbcInsert3AddCommentLike( 4398046511123L, 343597384736L, new Date( 1290725729770L ) ) );
        // add delete here
    }

    @Test
    public void testUpdateQuery4() throws Exception
    {
        run( db, new LdbcInsert4AddForum( 343597383803L, "Album 1 of Wolfgang Bauer", new Date( 1290883501867L ), 10, ImmutableList.of( 4844L ) ) );
        // add delete here
    }

    @Test
    public void testUpdateQuery5() throws Exception
    {
        run( db, new LdbcInsert5AddForumMembership( 343597383798L, 8796093022252L, new Date( 1290748277090L ) ) );
        // add delete here
    }

    @Test
    public void testUpdateQuery6() throws Exception
    {
        run( db, new LdbcInsert6AddPost(
                343597384592L,
                "photo343597384592.jpg",
                new Date( 1290883512867L ),
                "46.21.0.249",
                "Internet Explorer",
                "",
                "",
                0,
                10L,
                343597383803L,
                50L,
                ImmutableList.of()
        ) );
        // add delete here
    }

    @Test
    public void testUpdateQuery7() throws Exception
    {
        run( db, new LdbcInsert7AddComment(
                343597384747L,
                new Date( 1290689294243L ),
                "49.206.89.61",
                "Safari",
                "no way!",
                7,
                10995116277809L,
                0,
                -1,
                343597384736L,
                ImmutableList.of() ) );
        // add delete here
    }

    @Test
    public void testUpdateQuery8() throws Exception
    {
        run( db, new LdbcInsert8AddFriendship( 4398046511147L, 10995116277809L, new Date( 1290907550597L ) ) );
        // add delete here
    }
}

