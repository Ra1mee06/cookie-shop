package com.cookieshop.controller;

import com.cookieshop.entity.AdminInvite;
import com.cookieshop.entity.User;
import com.cookieshop.repository.AdminInviteRepository;
import com.cookieshop.util.AdminActionLogger;
import com.cookieshop.service.AdminInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/invites")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AdminInviteController {

	@Autowired
	private AdminInviteService adminInviteService;

	@Autowired
	private AdminInviteRepository adminInviteRepository;

	@Autowired
	private AdminActionLogger adminLogger;

	/** List all admin invites */
	@GetMapping
	public ResponseEntity<?> list(HttpServletRequest request) {
		User admin = adminLogger.verifyAdmin(request);
		List<AdminInvite> list = adminInviteRepository.findAll();
		adminLogger.log(admin, "Viewed all admin invites (" + list.size() + ")");
		return ResponseEntity.ok(list);
	}

	/** Create a new one-time admin invite code */
	@PostMapping
	public ResponseEntity<?> create(@RequestBody(required = false) Map<String, Object> body, HttpServletRequest request) {
		User admin = adminLogger.verifyAdmin(request);
		Integer expiresInHours = null;
		if (body != null && body.containsKey("expiresInHours")) {
			Object v = body.get("expiresInHours");
			try { expiresInHours = v == null ? null : Integer.parseInt(v.toString()); } catch (Exception ignore) {}
		}
		AdminInvite invite = adminInviteService.createInvite(admin.getId(), expiresInHours);
		adminLogger.log(admin, "Created admin invite code");
		Map<String, Object> resp = new HashMap<>();
		resp.put("success", true);
		resp.put("invite", invite);
		return ResponseEntity.ok(resp);
	}

	/** Delete/revoke an invite */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
		User admin = adminLogger.verifyAdmin(request);
		return adminInviteRepository.findById(id).map(invite -> {
			adminInviteRepository.delete(invite);
			adminLogger.log(admin, "Deleted admin invite id=" + id);
			return ResponseEntity.noContent().build();
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}
}
