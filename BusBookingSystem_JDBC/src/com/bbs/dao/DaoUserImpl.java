package com.bbs.dao;


import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import com.bbs.beans.Bus;
import com.bbs.beans.Feedback;
import com.bbs.beans.Ticket;
import com.bbs.beans.User;
import com.bbs.exceptions.AddUserException;
import com.bbs.exceptions.BookingFailedException;
import com.bbs.exceptions.CancelFailedException;
import com.bbs.exceptions.CustomException;
import com.bbs.exceptions.DeleteFailedException;
import com.bbs.exceptions.TicketRetrievalFailedException;
import com.bbs.exceptions.UpdateFailedException;

public class DaoUserImpl implements DaoUser {
	String url = "jdbc:mysql://localhost:3307/busbooking?user=root&password=root";
	Connection conn;
	java.sql.Statement stmt;
	PreparedStatement pstmt;
	ResultSet rs = null;
	User user = new User();
	public DaoUserImpl() {

		try {
			//load Driver
			java.sql.Driver div = new com.mysql.jdbc.Driver();
			DriverManager.registerDriver(div);

			//GetConnection
			conn = DriverManager.getConnection(url);
		} catch (SQLException e) {
			throw new CustomException("CantGetConnection");
		}

	}




	@Override
	public User createUser(User user) {
		try {

			//iSSUE SQL Query
			//Insert User into users Table
			String q = "Insert INTO Users_info values(?,?,?,?,?)";
			pstmt = conn.prepareStatement(q);
			pstmt.setInt(1,user.getUserId());
			pstmt.setString(5,user.getUserName());
			pstmt.setString(3,user.getEmail());
			pstmt.setString(4,user.getPassword());
			pstmt.setLong(2,user.getContact());
			int i = pstmt.executeUpdate();
			if(i>0)
			{
				return user;
			}
			else
			{
				return null;
			}

		}catch(Exception e) {
		
		}
		return null;
	}

	@Override
	public Boolean updateUser(User user,String password) {
		try {
			//Updating User
			//Issue SQL quERY
			String q2 = "update users_info set password=?,email=?,contact=? where user_id=? ";
			pstmt = conn.prepareStatement(q2);
			pstmt.setInt(4,user.getUserId());
			pstmt.setString(1,user.getPassword());
			pstmt.setString(2, user.getEmail());
			pstmt.setLong(3, user.getContact());
			int j = pstmt.executeUpdate();
			if(j>0)
			{

				return true;
			}
			else
			{

				return false;
			}

		}
		catch(Exception e) {
			throw new UpdateFailedException("FailedTOUpdate");
		}

	}

	public Boolean deleteUser(int userId,String password) {

		try {

			//issue sql query
			//Delete User Based on Id
			String q3 = "DELETE FROM USERs_INFO WHERE user_ID=? and password=?";
			pstmt = conn.prepareStatement(q3);

			pstmt.setInt(1,userId);

			pstmt.setString(2,password);
			int k = pstmt.executeUpdate();
			if(k>0)
			{
				return true;
			}
			else
			{
				return false;
				
			}


		}
		catch(Exception e)
		{
			throw new DeleteFailedException("DeleteFailed");
		}




	}

	@Override
	public Boolean loginUser(int userId, String password) {
		try {
			//Check UserId And Password
			//Issue SQL Query
			String q4 = "SELECT * FROM USERS_INFO WHERE USER_ID=? AND PASSWORD=?";
			pstmt = conn.prepareStatement(q4);
			pstmt.setInt(1, userId);
			pstmt.setString(2, password);
			rs = pstmt.executeQuery();

			if(rs.next())
			{

				return true;
			}
		}


		catch(Exception e) {
         throw new com.bbs.exceptions.LoginException("LoginFAILED");
		}
		return false;
	}

	@Override
	public User searchUser(int userId) {
		try
		{
			//Search User By Id
			//Issue sql query
			String q1 = "Select * from users_info where user_id=?";
			pstmt = conn.prepareStatement(q1);
			pstmt.setInt(1, userId);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				int userid = rs.getInt("user_id");
				String userName = rs.getString("username");
				String email = rs.getString("email");
				String password = rs.getString("password");
				long contact = rs.getLong("contact");
				user.setUserId(userId);
				user.setUserName(userName);
				user.setEmail(email);
				user.setPassword(password);
				user.setContact(contact);
				if(user!= null)
				{
					return user;
				}

			}
			return null;
		}
		catch(Exception e)
		{
        throw new CustomException("FailedToGetUser");
		}
		

	}

	//BookTicket
	@Override
	public Ticket bookTicket(Ticket ticket) {
		try {
			//Converting String to util Date
			java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(ticket.getDate());
			//Converting Util date to Java Date
			java.sql.Date date = new java.sql.Date(date1.getTime());

			int choiceBus = ticket.getBusId();
			//checking Availability
			int available = checkAvailability(choiceBus, date );
			//if available execute if loop
			if(available >= ticket.getNumberOfSeats())
			{
				int tickets = ticket.getNumberOfSeats();
				available = available-tickets;
				//update availability based on Ticket BOOked
				String q1 = "Update availability set availSeats=? where bus_id=? and journeyDate=?";
				pstmt = conn.prepareStatement(q1);
				pstmt.setInt(2, choiceBus);
				pstmt.setDate(3, date);
				pstmt.setInt(1, available);
				int update = pstmt.executeUpdate();

				if(update > 0)
				{
					//Insert Booking info in Booking table
					String q2 = "INSERT INTO booking_info(booking_id,bus_id,date,destination,numOfSeats,source,user_id,booking_datetime) VALUES(?,?,?,?,?,?,?,?)";
					PreparedStatement pstmt = conn.prepareStatement(q2, java.sql.Statement.RETURN_GENERATED_KEYS);
					Integer min = 1;
					Integer max = 300;
					Integer bookingid= (int) ((Math.random() * ((max - min) + 1)) + min);
					pstmt.setInt(1,bookingid );
					pstmt.setInt(2,choiceBus );
					Timestamp ts=new java.sql.Timestamp(new java.util.Date().getTime());
					pstmt.setInt(7,ticket.getUserId());
					pstmt.setDate(3, date);
					pstmt.setInt(5, tickets);
					pstmt.setTimestamp(8, ts);
					pstmt.setString(6, ticket.getSource());
					pstmt.setString(4, ticket.getDestination());
					int j = pstmt.executeUpdate();
					if(j>0)
					{
						//Getting Booking Info along with booking ID
						String q3="Select * from booking_info where booking_datetime=?";
						pstmt=conn.prepareStatement(q3);
						pstmt.setTimestamp(1,ts );
						rs=pstmt.executeQuery();
						if(rs.next())
						{
							int bookingId=rs.getInt("booking_id");
							int busId=rs.getInt("bus_id");
							Date jDate=rs.getDate("date");
							int seats=rs.getInt("numOfSeats");
							ticket.setBookingId(bookingId);
							ticket.setBusId(busId);
							ticket.setDate(jDate.toString());
							ticket.setNumberOfSeats(seats);
							return ticket;
						}

					}

				}
				else 
				{
					return null;
				}
			}
			else
			{
				CustomException excpetion = new CustomException(
						"CustomException:SeatsNotAvailable");
				excpetion.getMessage();
			}

		} catch (Exception e) {
//			CustomException excpetion = new CustomException(
//					"CustomException:BookingFailed");
//			excpetion.getMessage();
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Boolean cancelTicket(int bookingId) {
		Boolean state = false;
		try {
			//cANCEL tICKET
			//Issue Query
			Ticket ticket=getTicket(bookingId);
			int num = ticket.getNumberOfSeats();
			int busId = ticket.getBusId();
			//Delete or Cancel ticket fromm booking_info table
			String query = "Delete from booking_info where booking_id=?";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1,bookingId);
			int update = pstmt.executeUpdate();
			int available;
			if(update > 0)
			{
				//Get AVAILABLE SEATS
				String q1 = "Select * from availability where bus_id=?";
				pstmt = conn.prepareStatement(q1);
				pstmt.setInt(1, busId);
				rs = pstmt.executeQuery();
				if(rs.next())
				{
					//Update available Seats based on Tickets Cancelled
					available = rs.getInt("avail_seats");
					int availableNew = available+num;
					String q2 = "Update availability set avail_seats=? where bus_id=?";
					pstmt = conn.prepareStatement(q2);
					pstmt.setInt(2, busId);
					pstmt.setInt(1, availableNew);
					int inc = pstmt.executeUpdate();
					if(inc > 0 )
					{
						state = true;
					}
				}


			}

		} catch (SQLException e) {
			throw new CancelFailedException("NotAbleToCancel");
		}

		return state;
	}

	//View Ticket
	@Override
	public Ticket getTicket(int bookingId) {
		try {
			//Issue Query
			//Get Info from Booking_info
			String query = "select * from booking_info where booking_id=?";
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1,bookingId );
			rs = pstmt.executeQuery();
			Ticket ticket = null;
			while(rs.next()) {
				bookingId = rs.getInt("booking_id");
				int busId = rs.getInt("bus_id");
				int userId = rs.getInt("user_id");
				Date date = rs.getDate("date");
				int seats = rs.getInt("numOfSeats");
				String source=rs.getString("source");
				String destination=rs.getString("destination");
				ticket = new Ticket();
				ticket.setUserId(userId);
				ticket.setDate(date.toString());
				ticket.setBusId(busId);
				ticket.setNumberOfSeats(seats);
				ticket.setBookingId(bookingId);
				ticket.setSource(source);
				ticket.setDestination(destination);
				return ticket;
			}
		} catch (SQLException e) {
			throw new TicketRetrievalFailedException("GetTicketFailedException");
		}
		return null;
	}

	//Check availability based on busId and DATE
	@Override
	public Integer checkAvailability(int busId, Date date) {
		try {
			//IssueSql
			String q1 = "Select * from availability where bus_id=? and journeyDate=?";
			pstmt = conn.prepareStatement(q1);
			pstmt.setInt(1, busId);
			pstmt.setDate(2, date);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int available = rs.getInt("availSeats");
				return available;
			}


		} catch (SQLException e) {
			throw new CustomException("CheckingFailedException");

		}
		return null;

	}





	//Search Buses based on SOURCE DESTINATION AND DATE
	@Override
	public List<Bus> searchBus(String source, String destination , Date date) {

		//Issue Sql
		try {
			//QUERY TO JOIN AVAILABILITY AND BUS_INFO
			String query = "Select * from bus_info natural join availability "
					+ " where source = ? and destination = ? and journeyDate= ? ";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, source);
			pstmt.setString(2, destination);
			pstmt.setDate(3, date);
			rs = pstmt.executeQuery();
			List<Bus> buses  = new ArrayList<>();
			int i=0;
			while(rs.next())
			{
				Bus bus = new Bus();
				int busId = rs.getInt("bus_id");
				String busName = rs.getString("busName");
				source = rs.getString("source");
				destination = rs.getString("destination");
				String busType = rs.getString("busType");
				Double price =  rs.getDouble("price");
				int totalSeat=rs.getInt("totalSeats");
				bus.setBusId(busId);
				bus.setBusName(busName);
				bus.setBusType(busType);
				bus.setSource(source);
				bus.setDestination(destination);
				bus.setPrice(price);
				bus.setTotalSeats(totalSeat);
				buses.add(i,bus);
				i++;
			}
			return buses;
		} catch (SQLException e) {
			throw new CustomException("SearchBusException");
		}
	}




	//Give Feedback
	@Override
	public Boolean giveFeedBack(Feedback feedback) {
		//Storing suggestion in table
		String query = "INSERT INTO suggestion(sugg_id,feedback,user_id) values(?,?,?)";
		try {
			pstmt = conn.prepareStatement(query);
			Integer min = 1;
			Integer max = 300;
			Integer sugg_id= (int) ((Math.random() * ((max - min) + 1)) + min);
			pstmt.setInt(1,sugg_id);
			pstmt.setInt(2,feedback.getUserId());
			pstmt.setString(3, feedback.getFeedback());
			Integer insert = pstmt.executeUpdate();
			if(insert > 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
       
		return false;
	}



    //Get All Tickets booked by particular User Id
	@Override
	public List<Ticket> getAllTickets(int userId) {
		String query = "SELECT * FROM BOOKING_INFO where user_id = ? ";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, userId);
			rs = pstmt.executeQuery();
			int i =0;
			List<Ticket> tickets = new ArrayList<>();
			while(rs.next())
			{
				Ticket ticket = new Ticket();
				ticket.setBookingId(rs.getInt("booking_id"));
				ticket.setBusId(rs.getInt("bus_id"));
				ticket.setDate(rs.getDate("journey_date").toString());
				ticket.setDestination(rs.getString("destination"));
				ticket.setSource(rs.getString("source"));
				ticket.setUserId(rs.getInt("user_id"));
				tickets.add(i,ticket);
				i++;
			}
			return tickets;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
