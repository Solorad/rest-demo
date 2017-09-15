package com.example.morenkov.config;

import com.example.morenkov.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;


public class PatchingInterceptor implements ReaderInterceptor {
    private static final Logger log = LoggerFactory.getLogger(PatchingInterceptor.class);

    private final UriInfo uriInfo;

    /**
     * {@code PatchingInterceptor} injection constructor.
     *
     * @param uriInfo {@code javax.ws.rs.core.UriInfo} proxy instance.
     */
    public PatchingInterceptor(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext readerInterceptorContext)
            throws IOException, WebApplicationException {
        // retrieve account by calling
        try {
            // check for POST request
            if (uriInfo.getPathParameters().size() == 0) {
                return readerInterceptorContext.proceed();
            }
            Object resource = uriInfo.getMatchedResources().get(0);
            String accountId = uriInfo.getPathParameters().get("accountId").get(0);
            Method getAccountMethod = resource.getClass().getDeclaredMethod("getAccount", String.class);
            Response response = (Response) getAccountMethod.invoke(resource, accountId);
            if (response.getStatus() != 200) {
                log.error("User was not found");
                throw new UserNotFoundException("User was not found");
            }
            Object entity = response.getEntity();

            // Use the Jackson 2.x classes to convert both the incoming patch
            // and the current state of the object into a JsonNode / JsonPatch
            ObjectMapper mapper = new ObjectMapper();
            JsonNode serverState = mapper.valueToTree(entity);
            JsonNode patchAsNode = mapper.readValue(readerInterceptorContext.getInputStream(), JsonNode.class);
            JsonPatch patch = JsonPatch.fromJson(patchAsNode);


            // Apply the patch
            JsonNode result = patch.apply(serverState);

            // Stream the result & modify the stream on the readerInterceptor
            ByteArrayOutputStream resultAsByteArray = new ByteArrayOutputStream();
            mapper.writeValue(resultAsByteArray, result);
            readerInterceptorContext.setInputStream(new ByteArrayInputStream(resultAsByteArray.toByteArray()));

            // Pass control back to the Jersey code
            return readerInterceptorContext.proceed();
        } catch (NoSuchMethodException e) {
            log.error("No matching GET method on resource", e);
            throw new InternalServerErrorException("No matching GET method on resource");
        } catch (JsonPatchException e) {
            log.error("Error applying patch.", e);
            throw new InternalServerErrorException("Error applying patch.", e);
        } catch (Exception e) {
            log.error("Exception occurred", e);
            throw new WebApplicationException(e);
        }
    }
}
