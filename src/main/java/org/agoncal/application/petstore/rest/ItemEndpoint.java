package org.agoncal.application.petstore.rest;

import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.UriBuilder.fromResource;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.agoncal.application.petstore.model.Item;
import org.agoncal.application.petstore.util.Loggable;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Antonio Goncalves http://www.antoniogoncalves.org --
 */

@Stateless
@Path("/items")
@Loggable
@Api("Item")
public class ItemEndpoint {

    // ======================================
    // = Attributes =
    // ======================================

    @PersistenceContext(unitName = "applicationPetstorePU")
    private EntityManager entityManager;

    // ======================================
    // = Business methods =
    // ======================================

    @POST
    @Consumes({ "application/xml", "application/json" })
    @ApiOperation("Creates a new item")
    public Response create(Item entity) {
        entityManager.persist(entity);
        return Response.created(fromResource(ItemEndpoint.class).path(String.valueOf(entity.getId())).build()).build();
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    @ApiOperation("Deletes an item by its id")
    public Response deleteById(@PathParam("id") Long id) {
        Item entity = entityManager.find(Item.class, id);
        if (entity == null) {
            return status(NOT_FOUND).build();
        }
        entityManager.remove(entity);
        return noContent().build();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces({ "application/xml", "application/json" })
    @ApiOperation("Finds an item by its id")
    public Response findById(@PathParam("id") Long id) {
        TypedQuery<Item> findByIdQuery = entityManager.createQuery("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.product WHERE i.id = :entityId ORDER BY i.id",
                Item.class);
        findByIdQuery.setParameter("entityId", id);

        Item entity;
        try {
            entity = findByIdQuery.getSingleResult();
        } catch (NoResultException nre) {
            entity = null;
        }
        if (entity == null) {
            return status(NOT_FOUND).build();
        }

        return ok(entity).build();
    }

    @GET
    @Produces({ "application/xml", "application/json" })
    @ApiOperation("Lists all items")
    public List<Item> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult) {
        TypedQuery<Item> findAllQuery = entityManager.createQuery("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.product ORDER BY i.id", Item.class);
        if (startPosition != null) {
            findAllQuery.setFirstResult(startPosition);
        }
        if (maxResult != null) {
            findAllQuery.setMaxResults(maxResult);
        }

        return findAllQuery.getResultList();
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes({ "application/xml", "application/json" })
    @ApiOperation("Updates an item")
    public Response update(Item entity) {
        try {
            entity = entityManager.merge(entity);
        } catch (OptimisticLockException e) {
            return status(CONFLICT).entity(e.getEntity()).build();
        }

        return noContent().build();
    }
}
