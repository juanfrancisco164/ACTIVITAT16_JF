package com.example.activitat16_jf;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.List;


@Path("/bookings")
public class BookingResource {
    private static List<Booking> bookings;
    private static File xmlFile;

    static {
        init();
    }

    public static void init() {
        try {
            xmlFile = new File("C:\\Users\\juanf\\OneDrive\\Escritorio\\ACTIVITAT16_JF\\bookings.xml");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            BookingHandler bookingHandler = new BookingHandler();
            saxParser.parse(xmlFile, bookingHandler);
            bookings = bookingHandler.getBookings();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Booking> getBookingsJson() {
        return bookings;
    }

    @POST
    @Path("/post")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBooking(Booking booking) {
        bookings.add(booking);
        writeBookingsToXML();
        init();
        return Response.status(Response.Status.CREATED).entity(booking).build();
    }

    @PUT
    @Path("/update/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBooking(@PathParam("id") String id, Booking updatedBooking) {
        try {
            if (updatedBooking == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("La reserva actualizada es nula").build();
            }

            boolean found = false;
            for (Booking booking : bookings) {
                if (booking.getLocationNumber().equals(id)) {
                    booking.setClientId(updatedBooking.getClientId());
                    booking.setClientName(updatedBooking.getClientName());
                    booking.setAgencyId(updatedBooking.getAgencyId());
                    booking.setAgencyName(updatedBooking.getAgencyName());
                    booking.setPrice(updatedBooking.getPrice());
                    booking.setRoomType(updatedBooking.getRoomType());
                    booking.setHotelId(updatedBooking.getHotelId());
                    booking.setHotelName(updatedBooking.getHotelName());
                    booking.setArrivalDate(updatedBooking.getArrivalDate());
                    booking.setRoomNights(updatedBooking.getRoomNights());

                    found = true;
                    break;
                }
            }

            if (!found) {
                return Response.status(Response.Status.NOT_FOUND).entity("Reserva no encontrada para el ID proporcionado: " + id).build();
            }

            writeBookingsToXML();
            init();

            return Response.ok(bookings).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error al actualizar la reserva: " + e.getMessage()).build();
        }
    }


    @DELETE
    @Path("/delete/{id}")
    public Response deleteBooking(@PathParam("id") String id) {

        boolean removed = bookings.removeIf(booking -> booking.getLocationNumber().equals(id));

        if (removed) {
            writeBookingsToXML();
            init();
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private void writeBookingsToXML() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("bookings");
            doc.appendChild(rootElement);

            for (Booking booking : bookings) {
                Element bookingElement = doc.createElement("booking");
                rootElement.appendChild(bookingElement);

                bookingElement.setAttribute("location_number", booking.getLocationNumber());

                Element clientElement = doc.createElement("client");
                clientElement.setAttribute("id_client", booking.getClientId());
                clientElement.appendChild(doc.createTextNode(booking.getClientName()));
                bookingElement.appendChild(clientElement);

                Element agencyElement = doc.createElement("agency");
                agencyElement.setAttribute("id_agency", booking.getAgencyId());
                agencyElement.appendChild(doc.createTextNode(booking.getAgencyName()));
                bookingElement.appendChild(agencyElement);

                Element priceElement = doc.createElement("price");
                priceElement.appendChild(doc.createTextNode(String.valueOf(booking.getPrice())));
                bookingElement.appendChild(priceElement);

                Element roomElement = doc.createElement("room");
                roomElement.setAttribute("id_type", booking.getRoomType());
                roomElement.appendChild(doc.createTextNode(booking.getRoomType()));
                bookingElement.appendChild(roomElement);

                Element hotelElement = doc.createElement("hotel");
                hotelElement.setAttribute("id_hotel", booking.getHotelId());
                hotelElement.appendChild(doc.createTextNode(booking.getHotelName()));
                bookingElement.appendChild(hotelElement);

                Element checkInElement = doc.createElement("check_in");
                checkInElement.appendChild(doc.createTextNode(booking.getArrivalDate()));
                bookingElement.appendChild(checkInElement);

                Element roomNightsElement = doc.createElement("room_nights");
                roomNightsElement.appendChild(doc.createTextNode(String.valueOf(booking.getRoomNights())));
                bookingElement.appendChild(roomNightsElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);

            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }
}