drop policy if exists "Admins and managers insert notifications" on public.notifications;

create policy "Authenticated users insert notifications"
on public.notifications
for insert
to authenticated
with check (
    exists (
        select 1
        from public.users target_user
        where target_user.id = notifications.user_id
    )
);
